package de.dfki.core.sampling

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

/**
  *  Simple random sampling from the entire history
  *
  * @author behrouz
  */
class SimpleRandomSampler(rate: Double = 0.1) extends Sampler(rate = rate) {
  /**
    *
    * @param processedRDD list of all the historical rdds
    * @param spark SparkContext object
    */
  override def sample(processedRDD: ListBuffer[RDD[String]], spark: SparkContext) = {
    val indices = (0 to processedRDD.size).filter(_ => rand.nextDouble < rate).toList
    select(processedRDD, indices, spark)

  }

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = "entire-history"
}
