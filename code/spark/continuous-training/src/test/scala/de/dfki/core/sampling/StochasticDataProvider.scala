package de.dfki.core.sampling

import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class StochasticDataProvider extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {

  }

  override def afterEach() {

  }


  test("RollingWindow") {
    val rollingWindowProvider = new RollingWindowProvider(10)
    var indices = (0 to 8).toList
    assert(rollingWindowProvider.sampleIndices(indices) == indices)
    indices :+= 9
    assert(rollingWindowProvider.sampleIndices(indices) == indices)
    indices :+= 10
    assert(rollingWindowProvider.sampleIndices(indices) == (1 to 10).toList)
  }

//  test("CachingRollingWindow") {
//    val rollingWindowProvider = new RollingWindowProvider(10)
//    var indices = (0 to 8).toList
//    assert(rollingWindowProvider.cache(indices) == (indices, List()))
//    indices :+= 9
//    assert(rollingWindowProvider.cache(indices) == (List(9), List()))
//    indices :+= 10
//    assert(rollingWindowProvider.cache(rollingWindowProvider.sampleIndices(indices)) == (List(10), List(0)))
//    indices :+= 11
//    indices :+= 12
//    indices :+= 13
//    assert(rollingWindowProvider.cache(rollingWindowProvider.sampleIndices(indices)) == (List(11, 12, 13), List(1, 2, 3)))
//  }
}
