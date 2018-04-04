package de.dfki.ml.classification

import de.dfki.ml.optimization.gradient.LogisticGradient
import de.dfki.ml.optimization.updater.{SquaredL2Updater, Updater}
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * @author bede01.
  */
class LogisticRegressionWithSGD(stepSize: Double,
                                numIterations: Int,
                                regParam: Double,
                                miniBatchFraction: Double,
                                convergenceTol: Double,
                                fitIntercept: Boolean,
                                updater: Updater)
  extends StochasticGradientDescent[LogisticRegressionModel](
    stepSize,
    numIterations,
    regParam,
    miniBatchFraction,
    convergenceTol,
    fitIntercept,
    updater) with Serializable {

  def this() = this(1.0, 100, 0.1, 1.0, 1E-6, true, new SquaredL2Updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, 1.0, 1E-6, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, miniBatchFraction, 1E-6, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double) = this(stepSize, numIterations, regParam, miniBatchFraction, 1E-6, true, new SquaredL2Updater)


  override def gradientFunction = new LogisticGradient(fitIntercept, regParam)


  override def createModel(weights: Vector, intercept: Double) = {
    //logger.info(s"Creating a Logistic Regression model with features size: ${weights.size} and intercept: $intercept")
    new LogisticRegressionModel(weights, intercept).clearThreshold()
  }


  override def run(input: RDD[LabeledPoint], initialWeights: Vector): LogisticRegressionModel = {
    run(input, initialWeights, 0.0)
  }

}
