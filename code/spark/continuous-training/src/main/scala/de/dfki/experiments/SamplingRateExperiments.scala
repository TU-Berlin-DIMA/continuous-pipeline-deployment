package de.dfki.experiments

import de.dfki.core.sampling.{Sampler, TimeBasedSampler, UniformSampler, WindowBasedSampler}

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
object SamplingRateExperiments {

  case class Item(materialized: Boolean)

  def runExperiment(samplingMethod: Sampler, N: Int, m: Int, s: Int, slack: Int = 5): Unit = {
    var totalMaterialized = 0
    var total = 0

    var items: ListBuffer[Item] = new ListBuffer[Item]()
    for (i <- 1 to N) {
      val item = Item(true)
      items += item

      if (i % slack == 0) {
        val selectedItems = samplingMethod.sampleIndices(items.indices.toList).map(i => items(i))
        val materializedCount = selectedItems.count(_.materialized)
        totalMaterialized += materializedCount
        total += selectedItems.size
      }

      if (items.size > m) {
        val evictIndex = items.size - 1 - m
        items(evictIndex) = Item(false)
      }

    }

    println(s"sampler (${samplingMethod.name}), m ($m), s ($s), materialized: $totalMaterialized, total: $total")
  }

  def main(args: Array[String]): Unit = {
    val N = 12000
    val SLACK = 5
    for (m <- List(2400, 7200)) {
      for (s <- List(100, 720)) {
        for (sampler <- List(new UniformSampler(s), new TimeBasedSampler(s), new WindowBasedSampler(s, 6000))) {
          runExperiment(sampler, N, m, s, SLACK)
        }

      }

    }


  }

}
