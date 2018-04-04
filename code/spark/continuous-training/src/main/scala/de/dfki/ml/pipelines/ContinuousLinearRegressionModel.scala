package de.dfki.ml.pipelines

import de.dfki.ml.LinearAlgebra
import de.dfki.ml.classification.LinearRegressionWithSGD
import de.dfki.ml.optimization.updater.Updater
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class ContinuousLinearRegressionModel(private var stepSize: Double,
                                      private var numIterations: Int,
                                      private var regParam: Double,
                                      private var convergenceTol: Double,
                                      private var miniBatchFraction: Double,
                                      private var updater: Updater) extends Model {
  override val algorithm = new LinearRegressionWithSGD(stepSize = stepSize,
    numIterations = numIterations,
    regParam = regParam,
    convergenceTol = convergenceTol,
    miniBatchFraction = miniBatchFraction,
    fitIntercept = true,
    updater = updater)

  override protected def predictPoint(vector: Vector, weights: Vector, intercept: Double): Double = {
    LinearAlgebra.dot(weights, vector) + intercept
  }
}
