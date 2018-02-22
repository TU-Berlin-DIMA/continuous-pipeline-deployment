package de.dfki.core.sampling

/**
  * Window based sampling
  *
  * @author behrouz
  */
class WindowBasedSampler(rate: Double = 0.1,
                         window: Int = 100) extends Sampler(rate = rate) {

  override def sampleIndices(indices: List[Int]) = {
    val history = indices.size
    val start = math.max(0, history - window)

    (start until history).filter(_ => rand.nextDouble < rate).toList

  }

  override def name = s"window($window)"
}
