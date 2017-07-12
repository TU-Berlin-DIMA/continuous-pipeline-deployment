package de.dfki.ml.optimization

import org.apache.spark.mllib.linalg.Vector
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

}
