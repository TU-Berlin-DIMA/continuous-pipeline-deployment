package de.dfki.preprocessing.datasets

import de.dfki.utils.CommandLineParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author bede01
  */
object URLRepPreprocessing {
  val INPUT_PATH = "data/url-reputation/raw"
  val OUTPUT_PATH = "data/url-reputation/processed"
  val FILE_COUNT = 100
  val SAMPLING_RATE = 1.0

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("URL Data Preprocessing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    val inputPath = parser.get("input-path", INPUT_PATH)
    val outputPath = parser.get("output-path", OUTPUT_PATH)
    val fileCount = parser.getInteger("file-count", FILE_COUNT)
    val samplingRate = parser.getDouble("sampling-rate", SAMPLING_RATE)

    val data = {
      if (samplingRate < 1.0)
        sc.textFile(s"$inputPath/Day0.svm").sample(withReplacement = false, fraction = samplingRate, seed = 42)
      else
        sc.textFile(s"$inputPath/Day0.svm")
    }

    data.saveAsTextFile(s"$outputPath/initial-training/day=0")

    for (i <- 1 to 120) {
      val data = sc.textFile(s"$inputPath/Day$i.svm")
      data.repartition(fileCount).saveAsTextFile(s"$outputPath/stream/day=$i")
    }
  }


}
