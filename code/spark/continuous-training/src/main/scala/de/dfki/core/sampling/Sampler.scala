package de.dfki.core.sampling

import org.apache.log4j.Logger
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
                       val rand: Random = new Random(System.currentTimeMillis()),
                       val cachingEnabled: Boolean = true) {

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  /** provide logic for caching mechanism
    *
    * @param selected_indices indices selected for the next proactive training
    * @return a tupe of (what_indices_to_cache, what_indices_to_uncache)
    */
  def cache(selected_indices: List[Int]): (List[Int], List[Int])

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
    * @return
    */
  def sample[T](processedRDD: ListBuffer[RDD[T]]): List[RDD[T]] = {
    val indices = sampleIndices(processedRDD.indices.toList)
    if (indices.nonEmpty) {
      select(processedRDD, indices)
    } else {
      List()
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
                        indices: List[Int]): List[RDD[T]] = {
    if (cachingEnabled) {
      val (toCache, toEvict) = cache(indices)
      // cache new items
      toCache.map {
        i =>
          processedRDD(i).cache()
          processedRDD(i).count()
      }
      // evict
      toEvict.map {
        i => processedRDD(i).unpersist()
      }

    }
    indices.map(i => processedRDD(i))

  }
}
