package de.dfki.ml.pipelines

import de.dfki.ml.classification.StochasticGradientDescent
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, LabeledPoint}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
abstract class Model extends Serializable {
  @transient lazy val logger = Logger.getLogger(getClass.getName)

  /**
    *
    * @param data training data rdd
    */
  def train(data: RDD[LabeledPoint]) = {
    model = if (model.isEmpty) {
      Some(algorithm.run(data))
    } else {
      Some(algorithm.run(data, model.get.weights, model.get.intercept))
    }
  }

  val algorithm: StochasticGradientDescent[_ <: GeneralizedLinearModel]

  var model: Option[_ <: GeneralizedLinearModel] = None

  def predict(data: RDD[(Double, Vector)]): RDD[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    val intercept = model.get.intercept
    // broadcast the weight to all the nodes to reduce the communication
    val weights = data.context.broadcast(model.get.weights)
    data.mapPartitions { iter =>
      val w = weights.value
      iter.map(x => (x._1, predictPoint(x._2, w, intercept)))
    }
  }

  def predict(data: DStream[(Double, Vector)]): DStream[(Double, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    val intercept = model.get.intercept
    // broadcast the weight to all the nodes to reduce the communication
    val weights = data.context.sparkContext.broadcast(model.get.weights)
    data.mapPartitions { iter =>
      val w = weights.value
      iter.map(x => (x._1, predictPoint(x._2, w, intercept)))
    }
  }

  protected def predictPoint(vector: Vector, weights: Vector, intercept: Double): Double

  def setMiniBatchFraction(miniBatchFraction: Double): Unit = {
    this.algorithm.optimizer.setMiniBatchFraction(miniBatchFraction)
  }

  def setNumIterations(numIterations: Int) = {
    this.algorithm.optimizer.setNumIterations(numIterations)
  }

  def setConvergenceTol(convergenceTol: Double) = {
    this.algorithm.optimizer.setConvergenceTol(convergenceTol)
  }

  def getNumIterations = this.algorithm.optimizer.numIterations

  def getConvergedAfter = this.algorithm.optimizer.getConvergedAfter
}
