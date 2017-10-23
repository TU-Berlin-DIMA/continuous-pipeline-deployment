package de.dfki.ml.pipelines

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
trait Pipeline[I] {
  def train(data: RDD[I])

  def predict(data: RDD[I]): RDD[(Double, Double)]

  def predict(data: DStream[I]): DStream[(Double, Double)]

  def update(data: RDD[I])

  def withMaterialization: Boolean
}
