package de.dfki.ml.classification

import de.dfki.ml.optimization.gradient.LeastSquaresGradient
import de.dfki.ml.optimization.updater.Updater
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel}
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class LinearRegressionWithSGD(stepSize: Double,
                              numIterations: Int,
                              regParam: Double,
                              convergenceTol: Double,
                              miniBatchFraction: Double,
                              fitIntercept: Boolean,
                              updater: Updater)
  extends StochasticGradientDescent[LinearRegressionModel](
    stepSize = stepSize,
    numIterations = numIterations,
    regParam = regParam,
    convergenceTol = convergenceTol,
    miniBatchFraction = miniBatchFraction,
    fitIntercept = fitIntercept,
    updater = updater) with Serializable {


  override def gradientFunction = new LeastSquaresGradient(fitIntercept, regParam)


  override def createModel(weights: Vector, intercept: Double) = {
    new LinearRegressionModel(weights, intercept)
  }


  override def run(input: RDD[LabeledPoint], initialWeights: Vector): LinearRegressionModel = {
    run(input, initialWeights, 0.0)
  }

}
