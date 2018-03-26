package de.dfki.core.sampling

import breeze.linalg.DenseVector
import breeze.stats.distributions.Multinomial

/**
  * @author behrouz
  */
class TimeBasedSampler(size: Int = 100) extends Sampler {
  /**
    *
    * @param indices original indices
    * @return sampled indices
    */
  override def sampleIndices(indices: List[Int]) = {
    val sampleSize = math.min(indices.size, size)
    logger.info(s"Total rdds = ${indices.size}, Sample size = $sampleSize")
    val s = indices.sum.toDouble
    // indices starts from zero, we must increment them by one
    val weights = indices.map(_ + 1).map(i => i / s).toArray
    val mult = Multinomial(DenseVector(weights))

    mult.sample(Math.ceil(sampleSize).toInt).distinct.toList
  }

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = s"time_based-$size"
}
