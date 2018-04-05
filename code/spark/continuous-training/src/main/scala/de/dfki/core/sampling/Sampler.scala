package de.dfki.core.sampling

import org.apache.log4j.Logger
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

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  /** this is where by default the initial historical data rdd is stored.
    * if this index is selected to be in the next random sample, the user should
    * perform another sampling operation [[RDD.sample((withReplacement = false, rate))]] on
    * it since the size is big
    */
  val HISTORICAL_DATA_INDEX = 0


  /**
    *
    * @param indices original indices
    * @return sampled indices
    */
  def sampleIndices(indices: List[Int]): List[Int]

  /**
    * this method is called from other applications for returning a sample of the
    * data
    *
    * @param processedRDD list of historical rdds
    * @param spark        SparkContext object
    * @return
    */
  def sample[T](processedRDD: ListBuffer[RDD[T]], spark: SparkContext): Option[RDD[T]] = {
    val indices = sampleIndices(processedRDD.indices.toList)
    if(indices.nonEmpty) {
      Option(select(processedRDD, indices, spark))
    } else {
      None
    }
  }

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
  private def select[T](processedRDD: ListBuffer[RDD[T]],
                     indices: List[Int],
                     spark: SparkContext) = {
    val sample = if (indices.contains(HISTORICAL_DATA_INDEX)) {
      // TODO: 0.1 is hard coded figure out a way to make it better
      processedRDD.head.sample(withReplacement = false, 0.1) :: indices.map(i => processedRDD(i))
    } else {
      indices.map(i => processedRDD(i))
    }
    logger.info(s"Return ${indices.size} rdds in the sample")
    spark.union(sample)
  }
}
