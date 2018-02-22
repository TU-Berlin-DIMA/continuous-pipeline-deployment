package de.dfki.core.sampling

import breeze.linalg.DenseVector
import breeze.stats.distributions.Multinomial

/**
  * @author behrouz
  */
class TimeBasedSampler(rate: Double) extends Sampler(rate) {

  override def sampleIndices(indices: List[Int]) = {
    val size = indices.size
    val s = indices.sum.toDouble
    // indices starts from zero, we must increment them by one
    val weights = indices.map(_ + 1).map(i => i / s).toArray
    val sampleSize = size * rate
    val mult = Multinomial(DenseVector(weights))

    mult.sample(Math.ceil(sampleSize).toInt).distinct.toList
  }

  override def name = "time_based"
}
