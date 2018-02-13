package de.dfki.ml.optimization.gradient

import breeze.linalg.{DenseVector => BDV, Vector => BV}
import de.dfki.ml.LinearAlgebra
import org.apache.spark.mllib.linalg.{DenseVector, Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class HingeGradient(fitIntercept: Boolean, regParamL2: Double) extends BatchGradient {
  var numFeatures = 0

  override def compute(instances: RDD[(Double, Vector)], weights: Vector) = {
    val context = instances.context
    val broadCastWeights = context.broadcast(weights)
    val hingeAggregator = {
      val seqOp = (c: HingeAggregator, instance: (Double, Vector)) =>
        c.add(instance, broadCastWeights.value)
      val combOp = (c1: HingeAggregator, c2: HingeAggregator) => c1.merge(c2)

      instances.treeAggregate(new HingeAggregator(numFeatures, 2, fitIntercept))(seqOp, combOp)
    }
    val totalGradientArray = hingeAggregator.gradient.toArray

    val regVal = 0
    (hingeAggregator.loss + regVal, new BDV[Double](totalGradientArray))
  }

  override def setNumFeatures(size: Int) = {
    numFeatures = size
  }
}

// TODO: Fix this
class HingeAggregator(private val numFeatures: Int,
                      numClasses: Int,
                      fitIntercept: Boolean) extends Serializable {
  private var weightSum = 0.0
  private var lossSum = 0.0

  private val gradientSumArray =
    Array.ofDim[Double](if (fitIntercept) numFeatures + 1 else numFeatures)

  /**
    * Add a new training instance to this LogisticAggregator, and update the loss and gradient
    * of the objective function.
    *
    * @param instance     The instance of data point to be added.
    * @param coefficients The coefficients corresponding to the features.
    * @return This LogisticAggregator object.
    */
  def add(instance: (Double, Vector), coefficients: Vector): this.type = {
    val label = instance._1
    val features = instance._2
    require(numFeatures == features.size, s"Dimensions mismatch when adding new instance." +
      s" Expecting $numFeatures but got ${features.size}.")

    val coefficientsArray = coefficients match {
      case dv: DenseVector => dv.values
      case _ =>
        throw new IllegalArgumentException(
          s"coefficients only supports dense vector but got type ${coefficients.getClass}.")
    }
    val localGradientSumArray = gradientSumArray

    numClasses match {
      case 2 =>
        // For Binary Logistic Regression.
        val margin = - {
          var sum = 0.0
          features.foreachActive { (index, value) =>
            if (value != 0.0) {
              sum += coefficientsArray(index) * value
            }
          }
          sum + {
            if (fitIntercept) coefficientsArray(numFeatures) else 0.0
          }
        }

        val multiplier = 1.0 / (1.0 + math.exp(margin)) - label

        features.foreachActive { (index, value) =>
          if (value != 0.0) {
            localGradientSumArray(index) += multiplier * value
          }
        }

        if (fitIntercept) {
          localGradientSumArray(numFeatures) += multiplier
        }

        if (label > 0) {
          // The following is equivalent to log(1 + exp(margin)) but more numerically stable.
          lossSum += log1pExp(margin)
        } else {
          lossSum += log1pExp(margin) - margin
        }
      case _ =>
        new NotImplementedError("LogisticRegression with ElasticNet in ML package " +
          "only supports binary classification for now.")
    }
    weightSum += 1
    this
  }

  /**
    * Merge another LogisticAggregator, and update the loss and gradient
    * of the objective function.
    * (Note that it's in place merging; as a result, `this` object will be modified.)
    *
    * @param other The other LogisticAggregator to be merged.
    * @return This LogisticAggregator object.
    */
  def merge(other: HingeAggregator): this.type = {
    require(numFeatures == other.numFeatures, s"Dimensions mismatch when merging with another " +
      s"LeastSquaresAggregator. Expecting $numFeatures but got ${other.numFeatures}.")

    if (other.weightSum != 0.0) {
      weightSum += other.weightSum
      lossSum += other.lossSum

      var i = 0
      val localThisGradientSumArray = this.gradientSumArray
      val localOtherGradientSumArray = other.gradientSumArray
      val len = localThisGradientSumArray.length
      while (i < len) {
        localThisGradientSumArray(i) += localOtherGradientSumArray(i)
        i += 1
      }
    }
    this
  }

  def loss: Double = {
    require(weightSum > 0.0, s"The effective number of instances should be " +
      s"greater than 0.0, but $weightSum.")
    lossSum / weightSum
  }

  //TODO : investigate this , why the average was too big for SGD to work properly
  def gradient: Vector = {
    require(weightSum > 0.0, s"The effective number of instances should be " +
      s"greater than 0.0, but $weightSum.")
    val result = Vectors.dense(gradientSumArray.clone())
    LinearAlgebra.scal(1.0 / (weightSum * weightSum), result)
    result
  }

  /**
    * Copy from spark: @see org.apache.spark.mllib.util.MLUTILS#log1pExp
    *
    * @param x
    * @return
    */
  def log1pExp(x: Double): Double = {
    if (x > 0) {
      x + math.log1p(math.exp(-x))
    } else {
      math.log1p(math.exp(x))
    }
  }

}