package de.dfki.core.sampling

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class ZeroSampler extends Sampler {
  /**
    *
    * @param processedRDD list of all the historical rdds
    * @param spark
    */
  override def sample(processedRDD: ListBuffer[RDD[String]], spark: SparkContext) = spark.emptyRDD[String]

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = "empty-sample"
}
