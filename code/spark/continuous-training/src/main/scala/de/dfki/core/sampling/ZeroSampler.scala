package de.dfki.core.sampling

/**
  * @author behrouz
  */
class ZeroSampler extends Sampler {

  override def sampleIndices(indices: List[Int]) = {
    List[Int]()
  }

  override def name = "empty_sample"
}
