package de.dfki.preprocessing

import de.dfki.preprocessing.parsers.{CSVParser, SVMParser}
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class Preprocessor {

  def split(data: RDD[LabeledPoint], splits: Array[Double], partitions: Int = 100): (RDD[LabeledPoint], RDD[LabeledPoint]) = {
    val splitted = data.randomSplit(splits)
    (splitted(0), splitted(1).repartition(partitions))
  }

  def scale(data: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    val scaler = new StandardScaler(withMean = true, withStd = true).fit(data.map(a => a.features))
    data.map(d => new LabeledPoint(d.label, scaler.transform(d.features)))
  }

  def convertToCSV(data: RDD[LabeledPoint]): RDD[String] = {
    val parser = new CSVParser()
    data.map(parser.unparsePoint)
  }

  def convertToSVM(data: RDD[LabeledPoint]): RDD[String] = {
    val parser = new SVMParser(0)
    data.map(parser.unparsePoint)
  }

}
