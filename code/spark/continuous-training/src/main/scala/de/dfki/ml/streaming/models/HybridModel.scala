package de.dfki.ml.streaming.models

import java.io.{File, FileWriter}

import de.dfki.ml.classification.StochasticGradientDescent
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, LabeledPoint, StreamingLinearAlgorithm}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag

/**
  * @author behrouz
  */
abstract class


HybridModel[M <: GeneralizedLinearModel, A <: StochasticGradientDescent[M]]
  extends StreamingLinearAlgorithm[M, A] with Serializable {


  protected var model: Option[M]
  protected val algorithm: A

  def setModel(initialModel: M): this.type = {
    this.model = Some(initialModel)
    this
  }

  def trainInitialModel(rdd: RDD[LabeledPoint]): this.type = {
    this.model = Some(algorithm.run(rdd))
    this
  }

  /** Set the step size for gradient descent. Default: 0.1. */

  def setStepSize(stepSize: Double): this.type = {
    this.algorithm.optimizer.setStepSize(stepSize)
    this
  }

  /** Set the number of iterations of gradient descent to run per update. Default: 50. */
  def setNumIterations(numIterations: Int): this.type = {
    this.algorithm.optimizer.setNumIterations(numIterations)
    this
  }

  /** Set the fraction of each batch to use for updates. Default: 1.0. */
  def setMiniBatchFraction(miniBatchFraction: Double): this.type = {
    this.algorithm.optimizer.setMiniBatchFraction(miniBatchFraction)
    this
  }

  /** Set the regularization parameter. Default: 0.0. */
  def setRegParam(regParam: Double): this.type = {
    this.algorithm.optimizer.setRegParam(regParam)
    this
  }

  /**
    * Set the convergence tolerance
    *
    * @param convergenceTol convergence toll
    * @return
    */
  def setConvergenceTol(convergenceTol: Double): this.type = {
    this.algorithm.optimizer.setConvergenceTol(convergenceTol)
    this
  }


  def latestModelWeights() = super.latestModel().weights


  def predictOnValues[K: ClassTag](data: RDD[(K, Vector)]): RDD[(K, Double)] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    data.mapValues { x => model.get.predict(x) }
  }

  /**
    * Update the model based on a batch of data (static data)
    *
    * @param data RDD containing the training data to be used
    */
  def trainOn(data: RDD[LabeledPoint]): Unit = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }
    model = Some(algorithm.run(data, model.get.weights, model.get.intercept))
  }

  def writeToDisk(data: DStream[LabeledPoint], resultPath: String): Unit = {
    val storeErrorRate = (rdd: RDD[LabeledPoint]) => {
      val file = new File(s"$resultPath/model-parameters.txt")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        fw.write(s"${model.get.weights.toString}\n")
      }
      finally fw.close()
    }
    data.foreachRDD(storeErrorRate)
  }

  override def trainOn(observations: DStream[LabeledPoint]): Unit = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting training.")
    }
    observations.foreachRDD { (rdd, _) =>
      if (!rdd.isEmpty) {
        model = Some(algorithm.run(rdd, model.get.weights, model.get.intercept))
      }
    }
  }

  override def toString(): String = {
    model.get.toString()
  }


}
