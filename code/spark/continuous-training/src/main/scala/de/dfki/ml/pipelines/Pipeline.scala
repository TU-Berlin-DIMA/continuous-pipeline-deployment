package de.dfki.ml.pipelines

import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
trait Pipeline[I] {
  def train(data: RDD[I])

  def predict(data: RDD[I]): RDD[(Double, Double)]

  def update(data: RDD[I])
}
