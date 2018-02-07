package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object SamplingModes {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/day_0"
  val STREAM_PATH = "data/criteo-full/experiments/stream"
  //val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val EVALUATION_PATH = "prequential"
  val RESULT_PATH = "../../../experiment-results/criteo-full/sampling-mode/local"
  val INITIAL_PIPELINE = "data/criteo-full/pipelines/sampling-mode/init_500"
  val DELIMITER = ","
  val NUM_FEATURES = 3000
  val NUM_ITERATIONS = 1
  val SLACK = 10
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
    val numIterations = parser.getInteger("iterations", NUM_ITERATIONS)
    val slack = parser.getInteger("slack", SLACK)
    val samplingRate = parser.getDouble("sample", SAMPLING_RATE)
    val pipelineName = parser.get("pipeline", INITIAL_PIPELINE)
    val days = parser.get("days",DAYS).split(",").map(_.toInt)
    val dayDuration = parser.getInteger("day_duration",DAY_DURATION)

    val conf = new SparkConf().setAppName("Sampling Mode Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val noWindow = if (Files.exists(Paths.get(pipelineName))) {
      CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
    } else {
      val t = getPipeline(ssc.sparkContext, delimiter, numFeatures, numIterations, data)
      CriteoPipeline.saveToDisk(t, pipelineName)
      t
    }

    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = -1,
      daysToProcess = days).deploy(ssc, noWindow)



    val halfDayWindow = CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration/2,
      daysToProcess = days).deploy(ssc, halfDayWindow)


    val fullDayWindow = CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
      resultPath = s"$resultPath/continuous",
      samplingRate = samplingRate,
      slack = slack,
      windowSize = dayDuration,
      daysToProcess = days).deploy(ssc, fullDayWindow)

    val noSampling = CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
    new ContinuousDeploymentQualityAnalysis(history = inputPath,
      streamBase = streamPath,
      evaluation = s"$evaluationPath",
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
