package de.dfki.core.sampling

/**
  * @author behrouz
  */
class RollingWindowProvider(size: Int = 100) extends Sampler {
  var cachedIndices = List[Int]()

  /**
    *
    * @param indices original indices
    * @return sampled indices
    */
  override def sampleIndices(indices: List[Int]) = {
    val dataSize = indices.size
    val start = math.max(0, dataSize - size)
    indices.slice(start, dataSize)
  }

  /**
    * return a name for logging and experiment results recording
    *
    * @return
    */
  override def name = s"rolling_window-$size"

}
