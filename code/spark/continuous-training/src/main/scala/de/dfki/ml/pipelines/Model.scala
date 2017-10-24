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

  /**
    *
    * @param data      training data rdd
    * @param dimension optional parameter, if the model size changes during the deployment
    *                  this value should indicate the new dimension isze
    */
  def train(data: RDD[LabeledPoint], dimension: Int)

  /**
    * Expose this method because in offline and deployment mode the mini batch fraction is handled differently
    *
    * @param miniBatchFraction sampling rate of the dataset
    */
  def setMiniBatchFraction(miniBatchFraction: Double)

  def setNumIterations(numIterations: Int)
}
