package de.dfki.preprocessing.parsers
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * @author behrouz
  */
class DelimitedFileParser(delimiter: String) extends DataParser{
  override def parsePoint(point: String): LabeledPoint = {
    val values = point.split(delimiter).map(a => a.toDouble)
    new LabeledPoint(values(0), Vectors.dense(values.slice(1, 100)))
  }

  override def unparsePoint(p: LabeledPoint): String = {
    s"${p.label} $delimiter ${p.features.toArray.toList.mkString(delimiter)}"
  }
}
