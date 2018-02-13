package de.dfki.ml.optimization.gradient

import breeze.linalg.{Vector => BV}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
abstract class BatchGradient extends Serializable {

  def compute(data: RDD[(Double, Vector)], weights: Vector): (Double, BV[Double])

  def setNumFeatures(size: Int)
}

