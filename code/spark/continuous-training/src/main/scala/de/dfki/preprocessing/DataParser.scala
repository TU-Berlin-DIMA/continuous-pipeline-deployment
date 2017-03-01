package de.dfki.preprocessing

import org.apache.spark.mllib.regression.LabeledPoint

/**
  * @author bede01.
  */
trait DataParser extends Serializable {
  def parsePoint(p: String): LabeledPoint

}
