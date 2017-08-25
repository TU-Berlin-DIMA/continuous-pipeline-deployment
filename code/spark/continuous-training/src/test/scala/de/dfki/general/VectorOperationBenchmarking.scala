package de.dfki.general

import breeze.linalg.{DenseVector, axpy}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class VectorOperationBenchmarking extends FunSuite with BeforeAndAfterEach {
  
  //@Ignore
  ignore("axpy vs explicit") {
    val size = 1000000
    val N = 1000
    val v1 = DenseVector.rand[Double](size)
    var v2 = DenseVector.rand[Double](size)

    var start = System.currentTimeMillis()
    for (_ <- 1 to N) {
      v2 = v2 - v1
    }
    var end = System.currentTimeMillis()

    println(s"time for $N explicit operations: ${end - start}")
    val v3 = DenseVector.rand[Double](size)
    start = System.currentTimeMillis()
    for (_ <- 1 to N) {
      axpy(-1.0, v1, v3)
    }
    end = System.currentTimeMillis()
    println(s"time for $N axpy: ${end - start}")
  }

  //@Ignore
  test("axpy vs explicit correctness") {
    val size = 10
    val v1 = DenseVector.rand[Double](size)
    val v2 = DenseVector.rand[Double](size)

    // explicit
    val v3 = v2 - v1

    // built in axpy : v2 += -1 * v1
    axpy(-1.0, v1, v2)

    // result should be equal
    assert(v3 == v2)
  }

}
