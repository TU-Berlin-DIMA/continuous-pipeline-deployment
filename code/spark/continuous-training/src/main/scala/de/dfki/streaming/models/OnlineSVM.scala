package de.dfki.streaming.models


import java.io.{File, FileWriter}

import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{LabeledPoint, StreamingLinearAlgorithm}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag

/**
  * @author Behrouz Derakhshan
  */
class OnlineSVM(private var stepSize: Double,
                private var numIterations: Int,
                private var miniBatchFraction: Double,
                private var regParam: Double)
  extends StreamingLinearAlgorithm[SVMModel, SVMWithSGD]
    with Serializable {


  override protected var model: Option[SVMModel] = None
  override protected val algorithm: SVMWithSGD = new SVMWithSGD()


  def this() = this(0.1, 50, 1.0, 0.0)

  def setInitialModel(initialModel: SVMModel): this.type = {
    this.model = Some(initialModel)
    this
  }

  /**
    * Set the number of iterations of gradient descent to run per update. Default: 50.
    */
  def setNumIterations(numIterations: Int): this.type = {
    this.algorithm.optimizer.setNumIterations(numIterations)
    this
  }

  /**
    * Set the step size for gradient descent. Default: 0.1.
    */
  def setStepSize(stepSize: Double): this.type = {
    this.algorithm.optimizer.setStepSize(stepSize)
    this
  }


  /**
    * Set the fraction of each batch to use for updates. Default: 1.0.
    */
  def setMiniBatchFraction(miniBatchFraction: Double): this.type = {
    this.algorithm.optimizer.setMiniBatchFraction(miniBatchFraction)
    this
  }

  /**
    * Set the regularization parameter. Default: 0.0.
    */
  def setRegParam(regParam: Double): this.type = {
    this.algorithm.optimizer.setRegParam(regParam)
    this
  }

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
    model = Some(algorithm.run(data, model.get.weights))
    val display = model.get.weights.size match {
      case x if x > 100 => model.get.weights.toArray.take(100).mkString("[", ",", "...")
      case _ => model.get.weights.toArray.mkString("[", ",", "]")
    }
    logInfo(s"Current model: weights, ${display}")
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
    super.trainOn(observations)
  }

  override def toString(): String = {
    model.get.toString()
  }


}
