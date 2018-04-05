package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.experiments.profiles.Profile
import de.dfki.ml.optimization.updater.{SquaredL2UpdaterWithAdam, Updater}
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.urlrep.URLRepPipeline
import de.dfki.utils.CommandLineParser
import org.apache.log4j.Logger
import org.apache.spark.SparkContext

/**
  * @author behrouz
  */
abstract class Experiment {

  val defaultProfile: Profile

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def getParams(args: Array[String], profile: Profile): Params = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", profile.INPUT_PATH)
    val streamPath = parser.get("stream", profile.STREAM_PATH)
    val evaluationPath = parser.get("evaluation", profile.EVALUATION_PATH)
    val resultPath = parser.get("result", profile.RESULT_PATH)
    val delimiter = parser.get("delimiter", profile.DELIMITER)
    val numFeatures = parser.getInteger("features", profile.NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", profile.NUM_ITERATIONS)
    val slack = parser.getInteger("slack", profile.SLACK)
    val regParam = parser.getDouble("reg-param", profile.REG_PARAM)
    val pipelineLocation = parser.get("pipeline", profile.INITIAL_PIPELINE)
    val pipelineName = parser.get("pipeline_name", profile.PIPELINE_NAME)
    // format first_day,last_day
    val streamingDays = parser.get("days", profile.DAYS).split(",").map(_.toInt)
    val days = Array.range(streamingDays(0), streamingDays(1) + 1)
    val dayDuration = parser.getInteger("day_duration", profile.DAY_DURATION)
    val sampleSize = parser.getInteger("sample_size", profile.SAMPLE_SIZE)
    val convergenceTol = parser.getDouble("convergence_tol", profile.CONVERGENCE_TOL)
    val updater = Updater.getUpdater(parser.get("updater", profile.UPDATER))
    val stepSize = parser.getDouble("step_size", profile.STEP_SIZE)
    val miniBatch = parser.getDouble("mini_batch", profile.MINI_BATCH)

    Params(inputPath = inputPath,
      streamPath = streamPath,
      evaluationPath = evaluationPath,
      resultPath = resultPath,
      initialPipeline = pipelineLocation,
      delimiter = delimiter,
      numFeatures = numFeatures,
      numIterations = numIterations,
      slack = slack,
      days = days,
      sampleSize = sampleSize,
      dayDuration = dayDuration,
      pipelineName = pipelineName,
      regParam = regParam,
      convergenceTol = convergenceTol,
      updater = updater,
      stepSize = stepSize,
      miniBatch = miniBatch)
  }

  def getPipeline(spark: SparkContext, params: Params): Pipeline = {
    if (Files.exists(Paths.get(params.initialPipeline))) {
      if (params.pipelineName == "criteo") {
        CriteoPipeline.loadFromDisk(params.initialPipeline, spark)
      } else if (params.pipelineName == "url-rep") {
        URLRepPipeline.loadFromDisk(params.initialPipeline, spark)
      } else {
        throw new IllegalArgumentException(s"Pipeline ${params.pipelineName} has not been constructed!!!")
      }
    } else {
      val data = spark.textFile(params.inputPath).repartition(spark.defaultParallelism)
      if (params.pipelineName == "criteo") {
        val pipeline = new CriteoPipeline(spark,
          delim = params.delimiter,
          updater = new SquaredL2UpdaterWithAdam(),
          miniBatchFraction = params.miniBatch,
          numIterations = params.numIterations,
          numCategories = params.numFeatures,
          regParam = params.regParam)
        pipeline.updateTransformTrain(data, params.numIterations)
        CriteoPipeline.saveToDisk(pipeline, params.initialPipeline)
        pipeline
      } else if (params.pipelineName == "url-rep") {
        val pipeline = new URLRepPipeline(spark,
          updater = params.updater,
          miniBatchFraction = params.miniBatch,
          numIterations = params.numIterations,
          numCategories = params.numFeatures,
          regParam = params.regParam,
          convergenceTol = params.convergenceTol)
        pipeline.updateTransformTrain(data, params.numIterations)
        URLRepPipeline.saveToDisk(pipeline, params.initialPipeline)
        pipeline
      } else {
        throw new IllegalArgumentException(s"Pipeline ${params.pipelineName} has not been constructed!!!")
      }
    }
  }

}
