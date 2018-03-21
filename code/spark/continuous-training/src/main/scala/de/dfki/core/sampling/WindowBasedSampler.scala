package de.dfki.core.sampling

import scala.util.Random

/**
  * Window based sampling
  *
  * @author behrouz
  */
class WindowBasedSampler(size: Int = 100,
                         window: Int = 1000) extends Sampler(rate = size) {

  override def sampleIndices(indices: List[Int]) = {
    val history = indices.size
    val start = math.max(0, history - window)
    val slice = indices.slice(start, history)
    val sampleSize = math.min(slice.size, size)
    Random.shuffle(slice).take(sampleSize)
  }

  override def name = s"window($window)"
}
