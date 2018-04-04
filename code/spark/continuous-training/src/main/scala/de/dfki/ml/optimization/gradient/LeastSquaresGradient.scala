package de.dfki.ml.optimization.gradient

import breeze.linalg.{DenseVector => BDV}
import de.dfki.ml.LinearAlgebra
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class LeastSquaresGradient(fitIntercept: Boolean, regParamL2: Double) extends BatchGradient {
  var numFeatures = 0

  override def compute(instances: RDD[(Double, Vector)], weights: Vector) = {
    val context = instances.context
    val broadCastWeights = context.broadcast(weights)
    val leastSquaresAggregator = {
      val seqOp = (c: LeastSquaresAggregator, instance: (Double, Vector)) =>
        c.add(instance, broadCastWeights.value)
      val combOp = (c1: LeastSquaresAggregator, c2: LeastSquaresAggregator) => c1.merge(c2)

      instances.treeAggregate(new LeastSquaresAggregator(numFeatures, fitIntercept))(seqOp, combOp)
    }
    val totalGradient = leastSquaresAggregator.gradient.toArray

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

    (leastSquaresAggregator.loss + regVal, new BDV[Double](totalGradient))
  }

  override def setNumFeatures(size: Int) = {
    numFeatures = size
  }
}

class LeastSquaresAggregator(private val numFeatures: Int, fitIntercept: Boolean) extends Serializable {
  private var weightSum = 0.0
  private var lossSum = 0.0

  private val gradientSumArray = Array.ofDim[Double](if (fitIntercept) numFeatures + 1 else numFeatures)

  /**
    * Add a new training instance to this LeastSquaresAggregator, and update the loss and gradient
    * of the objective function.
    *
    * @param instance     The instance of data point to be added.
    * @param coefficients The coefficients corresponding to the features.
    * @return This LeastSquaresAggregator object.
    */
  def add(instance: (Double, Vector), coefficients: Vector): this.type = {
    val label = instance._1
    val features = instance._2
    require(numFeatures == features.size, s"Dimensions mismatch when adding new instance." +
      s" Expecting $numFeatures but got ${features.size}.")

    val coefficientsArray = coefficients.toArray
    val localGradientSumArray = gradientSumArray


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

    val diff = dotProduct - label
    lossSum += {
      features.foreachActive { (index, value) =>
        localGradientSumArray(index) += diff * value
        if (fitIntercept) {
          localGradientSumArray(numFeatures) += diff
        }
      }
      diff * diff / 2.0
    }

    weightSum += 1
    this
  }

  /**
    * Merge another LeastSquaresAggregator, and update the loss and gradient
    * of the objective function.
    * (Note that it's in place merging; as a result, `this` object will be modified.)
    *
    * @param other The other LeastSquaresAggregator to be merged.
    * @return This LeastSquaresAggregator object.
    */
  def merge(other: LeastSquaresAggregator): this.type = {
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