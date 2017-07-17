package de.dfki.ml.classification

import de.dfki.ml.optimization.SquaredL2Updater
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.optimization.Updater

/**
  * @author behrouz
  */
class SVMWithSGD(stepSize: Double,
                 numIterations: Int,
                 regParam: Double,
                 miniBatchFraction: Double,
                 standardization: Boolean,
                 fitIntercept: Boolean,
                 updater: Updater)
  extends StochasticGradientDescent[SVMModel](stepSize,
    numIterations,
    regParam,
    miniBatchFraction,
    standardization,
    fitIntercept,
    updater) with Serializable {

  def this() = this(1.0, 100, 0.1, 1.0, true, true, new SquaredL2Updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, 1.0, true, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, miniBatchFraction, true, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double) = this(stepSize, numIterations, regParam, miniBatchFraction, true, true, new SquaredL2Updater)


  override def gradientFunction = ???

  override protected def createModel(weights: Vector, intercept: Double): SVMModel = {
    println(s"Creating a SVM model with features size: ${weights.size} and intercept: $intercept")
    new SVMModel(weights, intercept)
  }


}
