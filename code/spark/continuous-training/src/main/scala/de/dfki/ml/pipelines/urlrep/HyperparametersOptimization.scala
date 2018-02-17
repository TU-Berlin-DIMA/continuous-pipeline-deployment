package de.dfki.ml.pipelines.urlrep

import de.dfki.ml.evaluation.ConfusionMatrix
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.utils.CommandLineParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object HyperparametersOptimization {
  val TRAINING_DATA = "data/url-reputation/processed/initial-training/day_0"
  val HOLDOUT_DATA = "data/url-reputation/processed/stream/day_1"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", TRAINING_DATA)
    val testPath = parser.get("test-path", HOLDOUT_DATA)

    val conf = new SparkConf().setAppName("URL Rep Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val miniBatches = List(0.1, 0.2)
    val stepSizes = List(0.001, 0.005, 0.01, 0.05, 0.1)
    val regParams = List(0.001, 0.05, 0.01, 0.05)
    var results: List[(Double, Double, Double, ConfusionMatrix)] = List()
    val spark = new SparkContext(conf)
    val rawTest = spark.textFile(testPath)
    for (mb <- miniBatches) {
      for (ss <- stepSizes) {
        for (rp <- regParams) {
          val urlRepPipeline = new URLRepPipeline(spark,
            numIterations = 2000,
            stepSize = ss,
            updater = new SquaredL2UpdaterWithAdam(),
            miniBatchFraction = mb,
            regParam = rp,
            numCategories = 300000)
          val rawTraining = spark.textFile(inputPath)
          urlRepPipeline.updateTransformTrain(rawTraining)
          val baseResult = urlRepPipeline.predict(rawTest)
          val cMatrix = ConfusionMatrix.fromRDD(baseResult)
          results = (mb, ss, rp, cMatrix) :: results
        }
      }
    }

    for (hp <- results) {
      println(s"mini-batch(${hp._1}), step-size(${hp._2}), reg-param(${hp._3}), matrix(${hp._4})")
    }
    //mini-batch(0.1), step-size(0.001), reg-param(0.001), matrix(accuracy(0.9741), precision(0.9716509988249119), recall(0.9531700288184438), f-measure(0.9623217922606925))
  }
}
