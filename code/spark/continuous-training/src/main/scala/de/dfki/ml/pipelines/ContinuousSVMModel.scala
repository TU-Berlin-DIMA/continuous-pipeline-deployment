package de.dfki.ml.pipelines

import de.dfki.ml.LinearAlgebra
import de.dfki.ml.classification.SVMWithSGD
import de.dfki.ml.optimization.updater.Updater
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class ContinuousSVMModel(private var stepSize: Double,
                         private var numIterations: Int,
                         private var regParam: Double,
                         private var convergenceTol: Double,
                         private var miniBatchFraction: Double,
                         private var updater: Updater) extends Model {

  private var threshold: Option[Double] = Some(0.0)

  def setThreshold(threshold: Double) = {
    this.threshold = Some(threshold)
  }


  val algorithm = new SVMWithSGD(stepSize = stepSize,
    numIterations = numIterations,
    regParam = regParam,
    convergenceTol = convergenceTol,
    miniBatchFraction = miniBatchFraction,
    fitIntercept = true,
    updater = updater)


  override protected def predictPoint(vector: Vector, weights: Vector, intercept: Double): Double = {
    val margin = LinearAlgebra.dot(weights, vector) + intercept
    threshold match {
      case Some(t) => if (margin > t) 1.0 else 0.0
      case None => margin
    }
  }
}
