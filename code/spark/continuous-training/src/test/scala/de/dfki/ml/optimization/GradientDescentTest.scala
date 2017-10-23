package de.dfki.ml.optimization

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class GradientDescentTest extends FunSuite with BeforeAndAfterEach {
  var optimizer: SGDOptimizer = _
  var sc: SparkContext = _

  override def beforeEach() {
    optimizer = new GradientDescent(100, 1.0, 0.0, 1.0, 1E-6, true, true,
      new LogisticGradient(true, 1.0),
      new SquaredL2Updater())
    val conf = new SparkConf()
      .setAppName("Test SGD Optimizer")
      .setMaster("local[*]")
    sc = new SparkContext(conf)


  }

  override def afterEach() {

  }

  test("statistics update") {
//    val first: RDD[(Double, Vector)] = sc.parallelize(List((1.0, new DenseVector(Array(0, 0, 0)))))
//    val second: RDD[(Double, Vector)] = sc.parallelize(List((1.0, new DenseVector(Array(2, 4, 6)))))
//    optimizer.updateStatistics(first)
//
//    assert(Array(0, 0, 0) sameElements optimizer.getStatistics("mean"))
//    assert(Array(0, 0, 0) sameElements optimizer.getStatistics("std"))
//    assert(Array(3) sameElements optimizer.getStatistics("size"))
//
//    optimizer.updateStatistics(second)
//
//    //optimizer.getStatistics("std").foreach(println)
//    assert(Array(1, 2, 3) sameElements optimizer.getStatistics("mean"))
//    //assert(Array(1, 2, 3) sameElements optimizer.getStatistics("std"))
//    assert(Array(3) sameElements optimizer.getStatistics("size"))
//
//    optimizer.updateStatistics(second)
//    assert(Array(4.0/3, 8.0/3, 4) sameElements optimizer.getStatistics("mean"))
//    //assert(Array(1, 2, 3) sameElements optimizer.getStatistics("std"))
//    assert(Array(3) sameElements optimizer.getStatistics("size"))
  }
}
