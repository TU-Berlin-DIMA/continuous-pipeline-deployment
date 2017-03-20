package de.dfki.preprocessing.datasets

import de.dfki.preprocessing.Preprocessor
import de.dfki.preprocessing.parsers.CSVParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author bede01.
  */
object HiggsPreprocessing {
  val INPUT_PATH = "data/higgs-sample/raw"
  val OUTPUT_PATH = "data/higgs-sample/preprocessed"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Data Scaling")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    val dataParser = new CSVParser()
    val input = parser.get("input-path", INPUT_PATH)
    val output = parser.get("output-path", OUTPUT_PATH)

    val data = sc.textFile(input).map(dataParser.parsePoint)

    val preprocessor = new Preprocessor()
    val scaledData = preprocessor.scale(data)

    preprocessor.convertToCSV(scaledData).saveAsTextFile(output)
  }
}
