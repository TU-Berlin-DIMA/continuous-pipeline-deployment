package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.urlrep.URLRepPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object SamplingModes {
  val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
  val STREAM_PATH = "data/url-reputation/processed/stream"
  val EVALUATION_PATH = "prequential"
  val RESULT_PATH = "../../../experiment-results/url-reputation/sampling-mode/"
  val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-1"
  val DELIMITER = ","
  val NUM_FEATURES = 30000
  val NUM_ITERATIONS = 1000
  val SLACK = 5
  val DAYS = "1,20"
  val SAMPLING_RATE = 0.1
  val DAY_DURATION = 100
  val PIPELINE_NAME = "url-rep"
  val REG_PARAM = 0.01

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
    val regParam = parser.getDouble("reg-param", REG_PARAM)
    val samplingRate = parser.getDouble("sample", SAMPLING_RATE)
    val pipelineLocation = parser.get("pipeline", INITIAL_PIPELINE)
    val pipelineName = parser.get("pipeline_name", PIPELINE_NAME)
    // format first_day,last_day
    val streamingDays = parser.get("days", DAYS).split(",").map(_.toInt)
    val days = Array.range(streamingDays(0), streamingDays(1) + 1)
    val dayDuration = parser.getInteger("day_duration", DAY_DURATION)

    val conf = new SparkConf().setAppName("Sampling Mode Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val noWindow = getPipeline(ssc.sparkContext,
      delimiter,
      numFeatures,
      numIterations,
      regParam,
      data,
      pipelineName,
      pipelineLocation)

    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = -1,
      daysToProcess = days).deploy(ssc, noWindow)


    val halfDayWindow = getPipeline(ssc.sparkContext,
      delimiter,
      numFeatures,
      numIterations,
      regParam,
      data,
      pipelineName,
      pipelineLocation)
    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration / 2,
      daysToProcess = days).deploy(ssc, halfDayWindow)


    val fullDayWindow = getPipeline(ssc.sparkContext,
      delimiter,
      numFeatures,
      numIterations,
      regParam,
      data,
      pipelineName,
      pipelineLocation)
    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration,
      daysToProcess = days).deploy(ssc, fullDayWindow)

    val noSampling = getPipeline(ssc.sparkContext,
      delimiter,
      numFeatures,
      numIterations,
      regParam,
      data,
      pipelineName,
      pipelineLocation)

    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = 0.0,
      slack = slack,
      windowSize = 0,
      daysToProcess = days).deploy(ssc, noSampling)
  }

  def getPipeline(spark: SparkContext,
                  delimiter: String,
                  numFeatures: Int,
                  numIterations: Int,
                  regParam: Double,
                  data: RDD[String],
                  pipelineName: String,
                  pipelineLocation: String): Pipeline = {
    if (Files.exists(Paths.get(pipelineLocation))) {
      if (pipelineName == "criteo") {
        CriteoPipeline.loadFromDisk(pipelineLocation, spark)
      } else if (pipelineName == "url-rep") {
        URLRepPipeline.loadFromDisk(pipelineLocation, spark)
      } else {
        throw new IllegalArgumentException(s"Pipeline $pipelineName has not been constructed!!!")
      }
    } else {
      if (pipelineName == "criteo") {
        val pipeline = new CriteoPipeline(spark,
          delim = delimiter,
          updater = new SquaredL2UpdaterWithAdam(),
          miniBatchFraction = 0.1,
          numIterations = numIterations,
          numCategories = numFeatures,
          regParam = regParam)
        pipeline.updateTransformTrain(data)
        CriteoPipeline.saveToDisk(pipeline, pipelineLocation)
        pipeline
      } else if (pipelineName == "url-rep") {
        val pipeline = new URLRepPipeline(spark,
          updater = new SquaredL2UpdaterWithAdam(),
          miniBatchFraction = 0.1,
          numIterations = numIterations,
          numCategories = numFeatures,
          regParam = regParam)
        pipeline.updateTransformTrain(data)
        URLRepPipeline.saveToDisk(pipeline, pipelineLocation)
        pipeline
      } else {
        throw new IllegalArgumentException(s"Pipeline $pipelineName has not been constructed!!!")
      }
    }
  }


}
