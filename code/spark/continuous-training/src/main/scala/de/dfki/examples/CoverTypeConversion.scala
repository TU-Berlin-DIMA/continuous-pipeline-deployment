package de.dfki.examples

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author Behrouz Derakhshan
  */
object CoverTypeConversion {

  def main(args: Array[String]) {

    val conf = new SparkConf().setMaster("local").setAppName("Cover Type Data Set Conversion")
    val sc = new SparkContext(conf)
    val dataPath = "data/cover-types/libsvm/covtype.libsvm.binary.scale"
    val trainData = MLUtils.loadLibSVMFile(sc, dataPath).map(lp => {
      new LabeledPoint(lp.label - 1, lp.features)
    })

    val rdds = trainData.randomSplit(Array(0.1, 0.1, 0.8))
    writeToFile(rdds(0), "data/cover-types/initial-training/")
    writeToFile(rdds(1), "data/cover-types/test/")
    writeToFile(rdds(2).repartition(200), "data/cover-types/stream-training/")
  }

  def writeToFile(data: RDD[LabeledPoint], path: String) = {
    data.map(p => (p.label, p.features.toDense)).map {
      p =>
        var out = p._1.toString
        for (v <- p._2.values) {
          out += ", " + v.toString
        }
        out
    }.saveAsTextFile(path)
  }

}
