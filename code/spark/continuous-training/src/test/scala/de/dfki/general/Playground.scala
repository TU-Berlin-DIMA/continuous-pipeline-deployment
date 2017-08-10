package de.dfki.general

import breeze.linalg.{DenseVector, Vector}
import breeze.numerics.sqrt
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class Playground extends FunSuite with BeforeAndAfterEach {
  test("Default Values") {
    var ones: Vector[Double] = DenseVector.ones[Double](10) + DenseVector.ones[Double](10) + DenseVector.ones[Double](10)
    var twos: Vector[Double] = DenseVector.ones[Double](10) + DenseVector.ones[Double](10)

    println(twos :* ones)
  }

  test("Operations") {
    var ones: Vector[Double] = DenseVector.ones[Double](10) + DenseVector.ones[Double](10) + DenseVector.ones[Double](10)
    var twos: Vector[Double] = DenseVector.ones[Double](10) + DenseVector.ones[Double](10)

    println((sqrt(ones + 1E-6) / sqrt(ones + 1E-6)) :* ones)
  }

}
