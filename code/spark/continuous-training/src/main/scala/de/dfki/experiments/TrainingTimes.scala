package de.dfki.experiments

import de.dfki.deployment.{ContinuousDeploymentNoOptimization, ContinuousDeploymentWithStatisticsUpdate, PeriodicalDeploymentNoOptimization, PeriodicalDeploymentWithStatisticsUpdate}
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * experiments for computing the training time using continuous and daily
  * with or without optimization
  *
  * @author behrouz
  */
object TrainingTimes {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/day_0"
  val STREAM_PATH = "data/criteo-full/experiments/stream"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/training-time/local"
  val DELIMITER = ","
  val NUM_FEATURES = 30000
  val NUM_ITERATIONS = 500
  val SLACK = 10


  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", NUM_ITERATIONS)
    val slack = parser.getInteger("slack", SLACK)

    val conf = new SparkConf().setAppName("Training Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

//    val continuousNoOptimization = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
//
//    new ContinuousDeploymentNoOptimization(history = inputPath,
//      stream = s"$streamPath/*",
//      eval = evaluationPath,
//      resultPath = s"$resultPath/continuous-no-opt",
//      samplingRate = 0.1,
//      slack = slack).deploy(ssc, continuousNoOptimization)
//
//    val continuousWithStatisticsUpdate = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
//
//    new ContinuousDeploymentWithStatisticsUpdate(history = inputPath,
//      stream = s"$streamPath/*",
//      eval = evaluationPath,
//      resultPath = s"$resultPath/continuous-stat-update",
//      samplingRate = 0.1,
//      slack = slack)
//      .deploy(ssc, continuousWithStatisticsUpdate)

    val periodicalNoOptimization = getPipeline(ssc.sparkContext, delimiter, numFeatures, numIterations, data)

    new PeriodicalDeploymentNoOptimization(history = inputPath,
      stream = s"$streamPath",
      eval = evaluationPath,
      resultPath = s"$resultPath/periodical-no-opt"
      ).deploy(ssc, periodicalNoOptimization)

    val periodicalWithStatisticsUpdate = getPipeline(ssc.sparkContext, delimiter, numFeatures, numIterations, data)

    new PeriodicalDeploymentWithStatisticsUpdate(history = inputPath,
      stream = s"$streamPath",
      eval = evaluationPath,
      resultPath = s"$resultPath/continuous-stat-update").deploy(ssc, periodicalWithStatisticsUpdate)


  }

  def getPipeline(spark: SparkContext, delimiter: String, numFeatures: Int, numIterations: Int, data: RDD[String]) = {
    val pipeline = new CriteoPipeline(spark,
      delim = delimiter,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 0.1,
      numIterations = numIterations,
      numCategories = numFeatures)
    pipeline.update(data)
    pipeline.train(data)

    pipeline

  }
}
