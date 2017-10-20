package de.dfki.ml.pipelines.criteo

import de.dfki.ml.LinearAlgebra
import de.dfki.ml.classification.LogisticRegressionWithSGD
import de.dfki.ml.pipelines.Model
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
class ModelTrainer(private var stepSize: Double,
                   private var numIterations: Int,
                   private var regParam: Double,
                   private var miniBatchFraction: Double,
                   private var updater: Updater) extends Model {

  protected val algorithm = new LogisticRegressionWithSGD(stepSize, numIterations, regParam, miniBatchFraction, updater)

  protected var model: Option[LogisticRegressionModel] = None

  algorithm.optimizer.convergenceTol = 0.0

  override def train(data: RDD[LabeledPoint]) = {
    model = if (model.isEmpty) {
      Some(algorithm.run(data))
    } else {
      Some(algorithm.run(data, model.get.weights, model.get.intercept))
    }
  }

  def predict(data: RDD[(Double, Vector)]): RDD[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    data.mapValues { x => predictPoint(x) }
  }

  def predict(data: DStream[(Double, Vector)]): DStream[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    data.mapValues { x => predictPoint(x) }
  }

  private def predictPoint(vector: Vector) = {
    val margin = LinearAlgebra.dot(model.get.weights, vector) + model.get.intercept
    val score = 1.0 / (1.0 + math.exp(-margin))
    model.get.getThreshold match {
      case Some(t) => if (score > t) 1.0 else 0.0
      case None => score
    }
  }
}
