package de.dfki.core.sampling

import breeze.linalg.DenseVector
import breeze.stats.distributions.Multinomial

/**
  * @author behrouz
  */
class TimeBasedSampler(size: Int = 100) extends Sampler {
  var cachedIndices = List[Int]()

  /**
    *
    * @param indices original indices
    * @return sampled indices
    */
  override def sampleIndices(indices: List[Int]) = {
    val sampleSize = math.min(indices.size, size)
    //logger.info(s"Total rdds = ${indices.size}, Sample size = $sampleSize")
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

  override def cache(selected_indices: List[Int]) = {
    if (cachedIndices.isEmpty) {
      cachedIndices = selected_indices
      (selected_indices, List[Int]())
    } else {
      val toCach = (selected_indices.toSet -- cachedIndices.toSet).toList
      val toEvict = (cachedIndices.toSet -- selected_indices.toSet).toList
      cachedIndices = selected_indices
      (toCach, toEvict)
    }
  }
}
