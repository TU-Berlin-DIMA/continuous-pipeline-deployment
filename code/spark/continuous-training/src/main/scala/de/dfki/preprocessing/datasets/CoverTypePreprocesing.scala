package de.dfki.preprocessing.datasets

import de.dfki.preprocessing.Preprocessor
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz derakhshan
  */
object CoverTypePreprocesing {
  val INPUT_PATH = "data/cover-types/libsvm"
  val OUTPUT_PATH = "data/cover-types"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val input = parser.get("input-path", INPUT_PATH)
    val output = parser.get("output-path", OUTPUT_PATH)

    val conf = new SparkConf().setMaster("local").setAppName("Cover Type Data Set Conversion")
    val sc = new SparkContext(conf)

    val trainData = MLUtils.loadLibSVMFile(sc, input).map(lp => {
      new LabeledPoint(lp.label - 1, lp.features)
    })

    val preprocessor = new Preprocessor()
    val scaledData = preprocessor.scale(trainData.map(a => new LabeledPoint(a.label, new DenseVector(a.features.toArray))))
    val splits = preprocessor.split(scaledData, Array(0.1, 0.9))
    preprocessor.convertToCSV(splits._1).saveAsTextFile(s"$output/initial-training/")
    preprocessor.convertToCSV(splits._2).repartition(100).saveAsTextFile(s"$output/stream-training/")

  }

}
