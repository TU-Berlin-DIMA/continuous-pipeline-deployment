package de.dfki.ml.pipelines

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

/**
  * @author behrouz
  */
trait Model extends Serializable {

  def predict(data: RDD[(Double, Vector)]): RDD[(Double, Double)]

  def predict(data: DStream[(Double, Vector)]): DStream[(Double, Double)]

  def train(data: RDD[LabeledPoint])
}
