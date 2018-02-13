package de.dfki.experiments

import de.dfki.deployment._
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
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
  val DAYS = "1"
  val DAY_DURATION = 100


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
    val days = parser.get("days", DAYS).split(",").map(_.toInt)
    val dayDuration = parser.getInteger("day_duration",DAY_DURATION)

    val conf = new SparkConf().setAppName("Training Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val continuous = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)

    new ContinuousDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = 0.1,
      slack = slack,
      daysToProcess = days,
      windowSize = dayDuration).deploy(ssc, continuous)


    val periodical = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)

    new PeriodicalDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/periodical",
      numIterations = numIterations,
      daysToProcess = days
    ).deploy(ssc, periodical)


  }

  def getPipeline(spark: SparkContext, delimiter: String, numFeatures: Int, numIterations: Int, data: RDD[String]) = {
    val pipeline = new CriteoPipeline(spark,
      delim = delimiter,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 0.1,
      numIterations = numIterations,
      numCategories = numFeatures)
    pipeline.updateTransformTrain(data)
    pipeline
  }
}
