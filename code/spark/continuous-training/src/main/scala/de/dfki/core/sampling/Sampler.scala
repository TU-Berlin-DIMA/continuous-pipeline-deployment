package de.dfki.core.sampling

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Base class for all the Samplers
  *
  * @author behrouz
  *
  */
abstract class Sampler(val rate: Double = 0.1,
                       val rand: Random = new Random(System.currentTimeMillis())) {

  /** this is where by default the initial historical data rdd is stored.
    * if this index is selected to be in the next random sample, the user should
    * perform another sampling operation [[RDD.sample((withReplacement = false, rate))]] on
    * it since the size is big
    */
  val HISTORICAL_DATA_INDEX = 0


  /**
    *
    * @param processedRDD list of all the historical rdds
    * @param spark
    */
  def sample(processedRDD: ListBuffer[RDD[String]], spark: SparkContext): RDD[String]

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  def name: String


  /**
    * getter for the [[rate]] parameter
    *
    * @return
    */
  def getRate = rate


  /**
    * Assemble the final sample
    *
    */
  protected def select(processedRDD: ListBuffer[RDD[String]],
                       indices: List[Int],
                       spark: SparkContext) = {
    val sample = if (indices.contains(HISTORICAL_DATA_INDEX)) {
      processedRDD.head.sample(withReplacement = false, rate) :: indices.map(i => processedRDD(i))
    } else {
      indices.map(i => processedRDD(i))
    }
    spark.union(sample)
  }
}
