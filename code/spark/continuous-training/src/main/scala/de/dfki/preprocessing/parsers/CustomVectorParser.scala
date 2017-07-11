package de.dfki.preprocessing.parsers

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * @author Behrouz
  */
class CustomVectorParser extends DataParser {
  override def parsePoint(point: String): LabeledPoint = {
    val values = point.split("\\|")
    new LabeledPoint(values.head.trim.toDouble, Vectors.parse(values(1).trim))
  }

  override def unparsePoint(p: LabeledPoint): String = {
    s"${p.label.toString} | ${p.features.toString}"
  }
}
