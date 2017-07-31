package de.dfki.ml.optimization

import org.apache.spark.mllib.linalg.{DenseVector, Vector}
import org.apache.spark.mllib.optimization.Optimizer
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
abstract class SGDOptimizer extends Optimizer {

  /**
    * Optimize on a dataset with initial weights and no intercept
    *
    * @param data           input data
    * @param initialWeights initial weight
    * @return weight vector
    */
  def optimize(data: RDD[(Double, Vector)], initialWeights: Vector): Vector

  /**
    * optimize on a dataset with initial weight and intercept
    *
    * @param data           input data
    * @param initialWeights initial weights
    * @param intercept      initial intercept
    * @return weight vector and intercept
    */
  def optimize(data: RDD[(Double, Vector)], initialWeights: Vector, intercept: Double): Vector

  /**
    * update the internal statistics of the optimizer
    * This is made explicit so the caller can choose when to call this method
    *
    * @param data the dataset the statistics should be updated based on
    */
  def updateStatistics(data: RDD[(Double, Vector)])


  /**
    * explicitly called by the test unit to transform the model into the non standardized
    * space
    *
    * @param weights model weights
    * @return weights in non standardized feature space
    */
  def unStandardize(weights: Vector): Vector

}
