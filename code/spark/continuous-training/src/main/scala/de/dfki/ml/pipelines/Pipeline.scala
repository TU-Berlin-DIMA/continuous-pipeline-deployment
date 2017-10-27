package de.dfki.ml.pipelines

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
trait Pipeline extends Serializable {

  val model: Model

  def train(data: RDD[String])

  def trainOnMaterialized(data: RDD[LabeledPoint])

  def predict(data: RDD[String]): RDD[(Double, Double)]

  def predict(data: DStream[String]): DStream[(Double, Double)]

  def update(data: RDD[String]): RDD[LabeledPoint]

  def setMaterialization(mat: Boolean)

  def newPipeline(): Pipeline
}
