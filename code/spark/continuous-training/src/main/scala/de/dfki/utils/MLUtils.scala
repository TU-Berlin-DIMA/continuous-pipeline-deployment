package de.dfki.utils

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * Created by bede01 on 24/11/16.
  */
object MLUtils {
  def parsePoint(p: String): LabeledPoint = {
    val values = p.split(",").map(a => a.toDouble)
    new LabeledPoint(values(0), Vectors.dense(values.slice(1, 100)))
  }

  def unparsePoint(l: LabeledPoint): String = {
    var s: String = ""
    s += l.label
    for (i <- 0 to l.features.size - 1) {
      s += ", "
      s += l.features.toArray(i)
    }
    s
  }


}
