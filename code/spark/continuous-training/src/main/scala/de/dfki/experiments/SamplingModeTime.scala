package de.dfki.experiments

import de.dfki.deployment.ContinuousDeploymentSamplingTimeAnalysis
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
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
  val RESULT_PATH = "../../../experiment-results/criteo-full/sampling-mode-time/local"
  val DELIMITER = ","
  val NUM_FEATURES = 3000
  val SLACK = 1
  val DAYS = "1,2"
  val SAMPLING_RATE = 0.1
  val DAY_DURATION = 100
  val MODE = "half"
  val ITER = 50

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val slack = parser.getInteger("slack", SLACK)
    val samplingRate = parser.getDouble("sample", SAMPLING_RATE)
    val days = parser.get("days", DAYS).split(",").map(_.toInt)
    val dayDuration = parser.getInteger("day_duration", DAY_DURATION)
    val mode = parser.get("mode", MODE)
    val iter = parser.getInteger("iter", ITER)

    val conf = new SparkConf().setAppName("Sampling Mode Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)


    if (mode == "half") {
      val halfDayWindow = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
      new ContinuousDeploymentSamplingTimeAnalysis(history = inputPath,
        streamBase = streamPath,
        resultPath = s"$resultPath",
        samplingRate = samplingRate,
        dayDuration = dayDuration,
        windowSize = dayDuration / 2,
        daysToProcess = days,
        iter = iter).deploy(ssc, halfDayWindow)
    }
    else if (mode == "full") {
      val fullDayWindow = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
      new ContinuousDeploymentSamplingTimeAnalysis(history = inputPath,
        streamBase = streamPath,
        resultPath = s"$resultPath",
        samplingRate = samplingRate,
        dayDuration = dayDuration,
        windowSize = dayDuration,
        daysToProcess = days,
        iter = iter).deploy(ssc, fullDayWindow)
    }
    else if (mode == "history") {
      val history = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
      new ContinuousDeploymentSamplingTimeAnalysis(history = inputPath,
        streamBase = streamPath,
        resultPath = s"$resultPath",
        samplingRate = samplingRate,
        dayDuration = dayDuration,
        windowSize = -1,
        daysToProcess = days,
        iter = iter).deploy(ssc, history)
    }
    else {
      val noSampling = getPipeline(ssc.sparkContext, delimiter, numFeatures, 1, data)
      new ContinuousDeploymentSamplingTimeAnalysis(history = inputPath,
        streamBase = streamPath,
        resultPath = s"$resultPath",
        samplingRate = 0.0,
        dayDuration = dayDuration,
        windowSize = 1,
        daysToProcess = days,
        iter = iter).deploy(ssc, noSampling)
    }

  }

  def getPipeline(spark: SparkContext, delimiter: String, numFeatures: Int, numIterations: Int, data: RDD[String]) = {
    val pipeline = new CriteoPipeline(spark,
      delim = delimiter,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 0.1,
      numIterations = numIterations,
      numCategories = numFeatures)
    //pipeline.updateTransformTrain(data)
    pipeline
  }


}