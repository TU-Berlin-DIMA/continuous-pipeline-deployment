package de.dfki.ml.optimization.gradient

import breeze.linalg.{DenseVector => BDV}
import de.dfki.ml.LinearAlgebra
import org.apache.spark.mllib.linalg.{Vector, Vectors}
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

      instances.treeAggregate(new HingeAggregator(numFeatures, fitIntercept))(seqOp, combOp)
    }
    val totalGradient= hingeAggregator.gradient.toArray

    val regVal = if (regParamL2 == 0.0) {
      0.0
    } else {
      var sum = 0.0
      weights.foreachActive { case (featureIndex, value) =>
        val isIntercept = fitIntercept && (featureIndex == numFeatures)
        if (!isIntercept) {
          sum += {
            totalGradient(featureIndex) += regParamL2 * value
            value * value
          }
        }
      }
      0.5 * regParamL2 * sum
    }
    broadCastWeights.destroy()

    (hingeAggregator.loss + regVal, new BDV[Double](totalGradient))
  }

  override def setNumFeatures(size: Int) = {
    numFeatures = size
  }
}

class HingeAggregator(private val numFeatures: Int, fitIntercept: Boolean) extends Serializable {
  private var weightSum = 0.0
  private var lossSum = 0.0

  private val gradientSumArray = Array.ofDim[Double](if (fitIntercept) numFeatures + 1 else numFeatures)

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

    val coefficientsArray = coefficients.toArray
    val localGradientSumArray = gradientSumArray

    // For Binary Logistic Regression.
    val dotProduct = {
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

    val labelScaled = 2 * label - 1.0

    lossSum += {
      if (1.0 > labelScaled * dotProduct) {
        features.foreachActive { (index, value) =>
          if (value != 0.0) {
            localGradientSumArray(index) += -labelScaled * value
          }
        }

        if (fitIntercept) {
          localGradientSumArray(numFeatures) += -labelScaled
        }
        1.0 - labelScaled * dotProduct
      } else {
        0.0
      }
    }

    weightSum += 1
    this
  }

  /**
    * Merge another HingeAggregator, and update the loss and gradient
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

  def gradient: Vector = {
    require(weightSum > 0.0, s"The effective number of instances should be " +
      s"greater than 0.0, but $weightSum.")
    val result = Vectors.dense(gradientSumArray.clone())
    LinearAlgebra.scal(1.0 / weightSum, result)
    result
  }

}