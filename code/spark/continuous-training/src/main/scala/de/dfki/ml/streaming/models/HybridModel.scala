package de.dfki.ml.streaming.models

import java.io._

import de.dfki.ml.classification.StochasticGradientDescent
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, LabeledPoint, StreamingLinearAlgorithm}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag

/**
  * @author behrouz
  */
abstract class HybridModel[M <: GeneralizedLinearModel, A <: StochasticGradientDescent[M]]
  extends StreamingLinearAlgorithm[M, A] with Serializable {


  protected var model: Option[M]
  protected val algorithm: A

  def setModel(initialModel: M): this.type = {
    this.model = Some(initialModel)
    this
  }

  def getUnderlyingModel: M = {
    this.model.get
  }

  def trainInitialModel(rdd: RDD[LabeledPoint]): this.type = {
    rdd.cache()
    rdd.count()
    this.model = Some(algorithm.run(rdd))
    this
  }

  /** Set the step size for gradient descent. Default: 0.1. */

  def setStepSize(stepSize: Double): this.type = {
    algorithm.optimizer.setStepSize(stepSize)
    this
  }

  /** Set the number of iterations of gradient descent to run per update. Default: 50. */
  def setNumIterations(numIterations: Int): this.type = {
    algorithm.optimizer.setNumIterations(numIterations)
    this
  }

  /** Set the fraction of each batch to use for updates. Default: 1.0. */
  def setMiniBatchFraction(miniBatchFraction: Double): this.type = {
    algorithm.optimizer.setMiniBatchFraction(miniBatchFraction)
    this
  }

  /** Set the regularization parameter. Default: 0.0. */
  def setRegParam(regParam: Double): this.type = {
    algorithm.optimizer.setRegParam(regParam)
    this
  }

  /**
    * Set the convergence tolerance
    *
    * @param convergenceTol convergence toll
    * @return
    */
  def setConvergenceTol(convergenceTol: Double): this.type = {
    algorithm.optimizer.setConvergenceTol(convergenceTol)
    this
  }

  /**
    * Set the updater
    *
    * @param updater updater
    * @return
    */
  def setUpdater(updater: Updater): this.type = {
    algorithm.optimizer.setUpdater(updater)
    this
  }

  /**
    * easy getter for accessing model parameters
    *
    * @return
    */
  def latestModelWeights() = super.latestModel().weights

  /**
    * use this instead of the [[predictOnValues(DStream)]]
    * It unstandardized the weights once for the entire rdd
    *
    * @param data input data
    * @tparam K label type (implict)
    * @return rdd of (label,prediction)
    */
  def predictOnValues[K: ClassTag](data: RDD[(K, Vector)]): RDD[(K, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    // the weights have to be un standardized before making a prediction
    val scaledWeights = algorithm.optimizer.unStandardize(model.get.weights)
    data.mapValues { x => predictPoint(x, scaledWeights, model.get.intercept) }
  }

  override def predictOnValues[K: ClassTag](data: DStream[(K, Vector)]): DStream[(K, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    // the weights have to be un standardized before making a prediction
    data.mapValues { x => predictPoint(x,
      algorithm.optimizer.unStandardize(model.get.weights),
      model.get.intercept) }
  }

  def predictPoint(data: Vector, weight: Vector, intercept: Double): Double

  /**
    * update the statistics of the underlying optimizer
    * designed to work with the [[org.apache.spark.streaming.StreamingContext.transform()]]
    *
    * @param observations stream of training observations
    * @return the same input rdd for downstream processing
    */
  def updateStatistics(observations: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }
    algorithm.optimizer.updateStatistics(observations.map(l => (l.label, l.features)))
    observations
  }

  /**
    * Similar to [[trainOn]]
    * designed to work with the [[org.apache.spark.streaming.StreamingContext.transform()]]
    * method instead
    *
    * @param observations stream of training observations
    * @return the same input rdd for downstream processing
    */
  def trainOn(observations: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }
    model = Some(algorithm.run(observations, model.get.weights, model.get.intercept))
    observations
  }

  /**
    * train the model on combined batch (history) and stream (fast) data
    * designed to work with the [[org.apache.spark.streaming.StreamingContext.transform()]]
    *
    * NOTE: do not call the update statistics method of the optimizer. Statistics are already
    * updated using these data
    *
    * @param fast    rdd of the streaming data
    * @param history rdd of the batch data
    * @return the same input rdd for downstream processing
    */
  def trainOnHybrid(fast: RDD[LabeledPoint], history: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }

    if (!history.getStorageLevel.useMemory) {
      history.cache()
    }
    model = Some(algorithm.run(fast.union(history), model.get.weights, model.get.intercept))
    history.unpersist()
    fast
  }

  /**
    * The incoming data are assumed to be new and never seen before
    * Therefore a call to to optimizer's updateStatistics method is required
    *
    * @param observations stream of training observations
    */
  override def trainOn(observations: DStream[LabeledPoint]): Unit = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }
    observations.foreachRDD {
      (rdd, _) =>
        if (!rdd.isEmpty) {
          algorithm.optimizer.updateStatistics(rdd.map(l => (l.label, l.features)))
          model = Some(algorithm.run(rdd, model.get.weights, model.get.intercept))
        }
    }
  }

  override def toString: String = {
    s"${this.getClass.getCanonicalName}\n" +
      s"Algorithm: ${algorithm.toString}\n" +
      s"Model: ${if (model == null) "Model not initialized yet" else model.get.toString()}"
  }

  val getType: String
}

object HybridModel {

  def saveToDisk(path: String, model: HybridModel[_, _]) = {
    val file = new File(path)
    file.getParentFile.mkdirs()
    file.createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(file, false))
    oos.writeObject(model)
    oos.close()
  }

  def loadFromDisk(path: String): HybridModel[_, _] = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    val model = ois.readObject.asInstanceOf[HybridModel[_, _]]
    ois.close()
    model
  }
}

