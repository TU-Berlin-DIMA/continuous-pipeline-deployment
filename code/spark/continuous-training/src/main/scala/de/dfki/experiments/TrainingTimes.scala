package de.dfki.experiments

import de.dfki.ml.optimization.AdvancedUpdaters
import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf

/**
  * experiments for computing the training time using continuous and daily
  * with or without optimization
  *
  * @author behrouz
  */
object TrainingTimes {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val STREAM_PATH = "data/criteo-full/experiments/stream/1"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/loss-new"
  val UPDATER = "adam"
  val DELIMITER = ","
  val NUM_FEATURES = 3000000
  val SLACK = 10

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val updater = AdvancedUpdaters.getUpdater(parser.get("updater", UPDATER))
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val slack = parser.getInteger("slack", SLACK)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
  }
}
