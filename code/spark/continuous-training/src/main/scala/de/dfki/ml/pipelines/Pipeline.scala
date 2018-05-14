package de.dfki.ml.pipelines

import de.dfki.ml.evaluation.Score
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
trait Pipeline extends Serializable {

  def setSparkContext(sc: SparkContext): Unit

  val model: Model

  def predict(data: RDD[String]): RDD[(Double, Double)]

  def predict(data: DStream[String]): DStream[(Double, Double)]

  def score(data: RDD[String]): Score

  def update(data: RDD[String])

  def transform(data: RDD[String]): RDD[LabeledPoint]

  def train(data: RDD[LabeledPoint], iterations: Int = 1)

  def updateTransformTrain(data: RDD[String], iterations: Int = 1)

  def updateAndTransform(data: RDD[String]): RDD[LabeledPoint]

  def newPipeline(): Pipeline

  def name(): String
}
