package de.dfki.ml.pipelines.criteo

import de.dfki.ml.LinearAlgebra
import de.dfki.ml.classification.LogisticRegressionWithSGD
import de.dfki.ml.pipelines.Model
import org.apache.log4j.Logger
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.{DenseVector, Vector}
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream


/**
  * @author behrouz
  */
class LRModel(private var stepSize: Double,
              private var numIterations: Int,
              private var regParam: Double,
              private var miniBatchFraction: Double,
              private var updater: Updater) extends Model {

  @transient lazy val logger = Logger.getLogger(getClass.getName)
  protected val algorithm = new LogisticRegressionWithSGD(stepSize, numIterations, regParam, miniBatchFraction, updater)

  protected var model: Option[LogisticRegressionModel] = None

  algorithm.optimizer.convergenceTol = 0.0

  override def train(data: RDD[LabeledPoint], dimension: Int = -1) = {
    model = if (model.isEmpty) {
      Some(algorithm.run(data))
    } else {
      val newWeights = dimension match {
        case -1 => model.get.weights
        case _ => val currentWeights = model.get.weights.toArray
          val diff = dimension - currentWeights.length
          new DenseVector(currentWeights ++ Array.fill[Double](diff)(0.0))
      }
      Some(algorithm.run(data, newWeights, model.get.intercept))
    }
  }

  def predict(data: RDD[(Double, Vector)]): RDD[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    val intercept = model.get.intercept
    // broadcast the weight to all the nodes to reduce the communication
    val weights = data.context.broadcast(model.get.weights)
    data.mapValues { x => predictPoint(x, weights.value, intercept) }
  }

  def predict(data: DStream[(Double, Vector)]): DStream[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    val intercept = model.get.intercept
    // broadcast the weight to all the nodes to reduce the communication
    val weights = data.context.sparkContext.broadcast(model.get.weights)
    data.mapValues { x => predictPoint(x, weights.value, intercept) }
  }

  private def predictPoint(vector: Vector, weights: Vector, intercept: Double) = {
    val margin = LinearAlgebra.dot(weights, vector) + intercept
    val score = 1.0 / (1.0 + math.exp(-margin))
    model.get.getThreshold match {
      case Some(t) => if (score > t) 1.0 else 0.0
      case None => score
    }
  }

  override def setMiniBatchFraction(miniBatchFraction: Double): Unit = {
    this.algorithm.optimizer.setMiniBatchFraction(miniBatchFraction)
  }

  override def setNumIterations(numIterations: Int) = {
    this.algorithm.optimizer.setNumIterations(numIterations)
  }
}
