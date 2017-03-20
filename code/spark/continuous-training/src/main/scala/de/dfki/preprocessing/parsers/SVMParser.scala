package de.dfki.preprocessing.parsers

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * @author bede01.
  */
class SVMParser(featureSize: Int) extends DataParser {

  override def parsePoint(point: String): LabeledPoint = {
    val items = point.split(' ')
    val label = items.head.toDouble
    val (indices, values) = items.tail.filter(_.nonEmpty).map { item =>
      val indexAndValue = item.split(':')
      val index = indexAndValue(0).toInt - 1
      // Convert 1-based indices to 0-based.
      val value = indexAndValue(1).toDouble
      (index, value)
    }.unzip

    // check if indices are one-based and in ascending order
    var previous = -1
    var i = 0
    val indicesLength = indices.length
    while (i < indicesLength) {
      val current = indices(i)
      require(current > previous, s"indices should be one-based and in ascending order;"
        + " found current=$current, previous=$previous; line=\"$line\"")
      previous = current
      i += 1
    }
    new LabeledPoint(label, Vectors.sparse(featureSize, indices, values))
  }

  override def unparsePoint(p: LabeledPoint): String = {
    val sb = new StringBuilder(p.label.toString)
    p.features.foreachActive { case (i, v) =>
      sb += ' '
      sb ++= s"${i + 1}:$v"
    }
    sb.mkString
  }

}


