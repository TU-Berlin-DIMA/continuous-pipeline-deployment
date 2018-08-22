package de.dfki.core.sampling

import scala.util.Random

/**
  * Window based sampling
  *
  * @author behrouz
  */
class WindowBasedSampler(size: Int = 100,
                         window: Int = 1000) extends Sampler(rate = size) {

  var cachedIndices = List[Int]()

  override def sampleIndices(indices: List[Int]) = {
    val history = indices.size
    val start = math.max(0, history - window)
    val slice = indices.slice(start, history)
    val sampleSize = math.min(slice.size, size)
    Random.shuffle(slice).take(sampleSize)
  }

  override def name = s"window($window)-$size"

  override def cache(selected_indices: List[Int]) = {
    if (cachedIndices.size < window) {
      cachedIndices = (0 to cachedIndices.size).toList
      (List[Int](), List[Int]())
    } else {
      //val toCache = (cachedIndices.last + 1 to selected_indices.last).toList
      val cacheEvictPoint = cachedIndices.last - window
      val toEvict = (cachedIndices.head until cacheEvictPoint).toList
      cachedIndices = (cacheEvictPoint to cachedIndices.last).toList
      (List[Int](), toEvict)
    }
  }
}
