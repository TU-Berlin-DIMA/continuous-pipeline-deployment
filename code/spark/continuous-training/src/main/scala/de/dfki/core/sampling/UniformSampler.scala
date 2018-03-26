package de.dfki.core.sampling

import scala.util.Random

/**
  * @author behrouz
  */
class UniformSampler(size: Int = 100) extends Sampler {
  /**
    *
    * @param indices original indices
    * @return sampled indices
    */
  override def sampleIndices(indices: List[Int]) = {
    val sampleSize = math.min(indices.size, size)
    logger.info(s"Total rdds = ${indices.size}, Sample size = $sampleSize")
    Random.shuffle(indices).take(sampleSize)
  }

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = s"uniform-$size"
}
