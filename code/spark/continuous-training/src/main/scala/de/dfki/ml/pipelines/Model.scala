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
    * @param data training data rdd
    */
  def train(data: RDD[LabeledPoint])

  /**
    * Expose this method because in offline and deployment mode the mini batch fraction is handled differently
    *
    * @param miniBatchFraction sampling rate of the dataset
    */
  def setMiniBatchFraction(miniBatchFraction: Double)

  def setNumIterations(numIterations: Int)

  def setConvergenceTol(convergenceTol: Double)

  def getNumIterations: Int
}
