package de.dfki.preprocessing.parsers

import org.apache.spark.mllib.regression.LabeledPoint

/**
  * @author bede01.
  */
trait DataParser extends Serializable {
  def parsePoint(p: String): LabeledPoint

  def unparsePoint(p: LabeledPoint): String
}
