package de.dfki.experiments

import de.dfki.deployment.ContinuousDeploymentTimeAnalysis
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object SamplingModeTime {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/day_0"
  val STREAM_PATH = "data/criteo-full/experiments/stream"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/sampling-mode-time/local"
  val DELIMITER = ","
  val NUM_FEATURES = 3000
  val SLACK = 5
  val DAYS = "1,2,3,4,5"
  val SAMPLING_RATE = 0.1
  val DAY_DURATION = 100

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val slack = parser.getInteger("slack", SLACK)
    val samplingRate = parser.getDouble("sample", SAMPLING_RATE)
    val days = parser.get("days", DAYS).split(",").map(_.toInt)
    val dayDuration = parser.getInteger("day_duration", DAY_DURATION)

    val conf = new SparkConf().setAppName("Sampling Mode Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val noWindow = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)


    new ContinuousDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = -1,
      daysToProcess = days).deploy(ssc, noWindow)


    val halfDayWindow = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
    new ContinuousDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration / 2,
      daysToProcess = days).deploy(ssc, halfDayWindow)


    val fullDayWindow = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
    new ContinuousDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration,
      daysToProcess = days).deploy(ssc, fullDayWindow)

    val noSampling = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
    new ContinuousDeploymentTimeAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluationPath = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = 0.0,
      slack = slack,
      windowSize = 0,
      daysToProcess = days).deploy(ssc, noSampling)
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