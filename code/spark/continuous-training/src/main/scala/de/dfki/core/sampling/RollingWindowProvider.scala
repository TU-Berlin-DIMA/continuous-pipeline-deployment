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

  override def cache(selected_indices: List[Int]) = {
    if (cachedIndices.isEmpty) {
      cachedIndices = selected_indices
      (selected_indices, List[Int]())
    } else {
      //val toCache = (cachedIndices.last + 1 to selected_indices.last).toList
      val toEvict = (cachedIndices.head until selected_indices.head).toList
      cachedIndices = selected_indices
      (List[Int](), toEvict)
    }
  }
}
