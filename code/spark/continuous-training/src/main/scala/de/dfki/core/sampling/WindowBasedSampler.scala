package de.dfki.core.sampling

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

/**
  * Window based sampling
  *
  * @author behrouz
  */
class WindowBasedSampler(rate: Double = 0.1,
                         window: Int = 100) extends Sampler(rate = rate) {
  /**
    *
    * @param processedRDD list of all the historical rdds
    * @param spark        SparkContext object
    */
  override def sample(processedRDD: ListBuffer[RDD[String]], spark: SparkContext) = {
    val history = processedRDD.size
    val start = math.max(0, history - window)

    val indices = (start to history).filter(_ => rand.nextDouble < rate).toList
    select(processedRDD, indices, spark)
  }

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = s"window($window)"
}
