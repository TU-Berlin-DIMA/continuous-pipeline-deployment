package de.dfki.utils

import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * Created by bede01 on 29/12/16.
  */
class MLUtils$Test extends FunSuite with BeforeAndAfterEach {


  test("testUnparsePoint") {
    val l = new LabeledPoint(1.0, new DenseVector(Array(1.0, 1, 3, 4, 5)))
    assert(MLUtils.unparsePoint(l) === "1.0, 1.0, 1.0, 3.0, 4.0, 5.0")
  }

  test("testParsePoint") {
    val parsed = MLUtils.parsePoint("1, 0, 0, 0, 1, 12, 0.1, 0,2")
    val expected = new LabeledPoint(1.0, new DenseVector(Array(0, 0, 0, 1, 12, 0.1, 0, 2)))

    assert(parsed === expected)

  }

}
