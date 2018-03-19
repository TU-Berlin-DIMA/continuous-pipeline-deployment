package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.core.sampling.{SimpleRandomSampler, TimeBasedSampler, WindowBasedSampler, ZeroSampler}
import de.dfki.deployment.{ContinuousDeploymentQualityAnalysis, OnlineDeploymentQualityAnalysis}
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
  val RESULT_PATH = "../../../experiment-results/url-reputation/sampling-moreiters"
  val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-3000"
  val DELIMITER = ","
  // URL FEATURE SIZE
  // val NUM_FEATURES = 3231961
  val NUM_FEATURES = 3000
  val NUM_ITERATIONS = 2000
  val SLACK = 5
  // 44 no error all 4400 rows are ok
  // 45 error but 3900 rows are only ok
  val DAYS = "1,120"
  val SAMPLING_RATE = 0.1
  val DAY_DURATION = 100
  val PIPELINE_NAME = "url-rep"
  val REG_PARAM = 0.001

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
    //
    //    val simpleSampling = getPipeline(ssc.sparkContext,
    //      delimiter,
    //      numFeatures,
    //      numIterations,
    //      regParam,
    //      data,
    //      pipelineName,
    //      pipelineLocation)
    //
    //    new ContinuousDeploymentQualityAnalysis(history = inputPath,
    //      streamBase = streamPath,
    //      evaluation = s"$evaluationPath",
    //      resultPath = s"$resultPath/continuous",
    //      daysToProcess = days,
    //      slack = slack,
    //      sampler = new SimpleRandomSampler(samplingRate)).deploy(ssc, simpleSampling)
    //
    val timeBased = getPipeline(ssc.sparkContext,
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
      daysToProcess = days,
      slack = slack,
      sampler = new TimeBasedSampler(samplingRate)).deploy(ssc, timeBased)

//    val online = getPipeline(ssc.sparkContext,
//      delimiter,
//      numFeatures,
//      numIterations,
//      regParam,
//      data,
//      pipelineName,
//      pipelineLocation)
//
//    new OnlineDeploymentQualityAnalysis(history = inputPath,
//      streamBase = streamPath,
//      evaluation = s"$evaluationPath",
//      resultPath = s"$resultPath/continuous",
//      daysToProcess = days).deploy(ssc, online)

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
        pipeline.updateTransformTrain(data, numIterations)
        CriteoPipeline.saveToDisk(pipeline, pipelineLocation)
        pipeline
      } else if (pipelineName == "url-rep") {
        val pipeline = new URLRepPipeline(spark,
          updater = new SquaredL2UpdaterWithAdam(),
          miniBatchFraction = 0.1,
          numIterations = numIterations,
          numCategories = numFeatures,
          regParam = regParam)
        pipeline.updateTransformTrain(data, numIterations)
        URLRepPipeline.saveToDisk(pipeline, pipelineLocation)
        pipeline
      } else {
        throw new IllegalArgumentException(s"Pipeline $pipelineName has not been constructed!!!")
      }
    }
  }


}
