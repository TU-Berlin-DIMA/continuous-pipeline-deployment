package de.dfki.ml.pipelines

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
trait Pipeline extends Serializable{

  val model: Model

  def train(data: RDD[String])

  def predict(data: RDD[String]): RDD[(Double, Double)]

  def predict(data: DStream[String]): DStream[(Double, Double)]

  def update(data: RDD[String])

  def withMaterialization: Boolean
}
