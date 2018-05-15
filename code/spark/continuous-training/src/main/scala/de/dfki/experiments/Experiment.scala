package de.dfki.experiments

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import de.dfki.experiments.profiles.Profile
import de.dfki.ml.optimization.updater.Updater
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.nyc_taxi.NYCTaxiPipeline
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


  def storeTime(time: Long, root: String, name: String = "time") = {
    val file = new File(s"$root/$name")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$time\n")
    }
    finally fw.close()
  }

  def getParams(args: Array[String], profile: Profile): Params = {
    val parser = new CommandLineParser(args).parse()
    val profileFromArgs = Profile.getProfile(parser.get("profile"), profile)
    val inputPath = parser.get("input", profileFromArgs.INPUT_PATH)
    val streamPath = parser.get("stream", profileFromArgs.STREAM_PATH)
    val materializedPath = parser.get("stream", profileFromArgs.MATERIALIZED_PATH)
    val evaluationPath = parser.get("evaluation", profileFromArgs.EVALUATION_PATH)
    val resultPath = parser.get("result", profileFromArgs.RESULT_PATH)
    val delimiter = parser.get("delimiter", profileFromArgs.DELIMITER)
    val numFeatures = parser.getInteger("features", profileFromArgs.NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", profileFromArgs.NUM_ITERATIONS)
    val slack = parser.getInteger("slack", profileFromArgs.SLACK)
    val regParam = parser.getDouble("reg-param", profileFromArgs.REG_PARAM)
    val pipelineLocation = parser.get("pipeline", profileFromArgs.INITIAL_PIPELINE)
    val pipelineName = parser.get("pipeline_name", profileFromArgs.PIPELINE_NAME)
    // format first_day,last_day
    val streamingDays = parser.get("days", profileFromArgs.DAYS).split(",").map(_.toInt)
    val days = Array.range(streamingDays(0), streamingDays(1) + 1)
    val dayDuration = parser.getInteger("day-duration", profileFromArgs.DAY_DURATION)
    val sampleSize = parser.getInteger("sample-size", profileFromArgs.SAMPLE_SIZE)
    val convergenceTol = parser.getDouble("convergence-tol", profileFromArgs.CONVERGENCE_TOL)
    val updater = Updater.getUpdater(parser.get("updater", profileFromArgs.UPDATER))
    val stepSize = parser.getDouble("step-size", profileFromArgs.STEP_SIZE)
    val miniBatch = parser.getDouble("mini-batch", profileFromArgs.MINI_BATCH)
    val batchEvaluationSet = parser.get("eval-set", profileFromArgs.BATCH_EVALUATION)
    val numPartitions = parser.getInteger("partitions", profileFromArgs.NUM_PARTITIONS)
    val trainingFrequency = parser.getInteger("training-frequency", profileFromArgs.TRAINING_FREQUENCY)
    val failedPipeline = parser.get("failed-pipeline", profileFromArgs.FAILED_PIPELINE)
    val initTime = parser.getInteger("init-time", profileFromArgs.INIT_TIME)

    Params(inputPath = inputPath,
      streamPath = streamPath,
      materializedPath = materializedPath,
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
      miniBatch = miniBatch,
      batchEvaluationSet = batchEvaluationSet,
      numPartitions = numPartitions,
      trainingFrequency = trainingFrequency,
      failedPipeline = failedPipeline,
      initTime = initTime)
  }

  def getPipeline(spark: SparkContext, params: Params): Pipeline = {
    if(params.failedPipeline != "None"){
      logger.info(s"loading a failed pipeline from: ${params.failedPipeline}")
      if (params.pipelineName == "criteo") {
        CriteoPipeline.loadFromDisk(params.failedPipeline, spark)
      } else if (params.pipelineName == "url-rep") {
        URLRepPipeline.loadFromDisk(params.failedPipeline, spark)
      } else if (params.pipelineName == "taxi") {
        NYCTaxiPipeline.loadFromDisk(params.failedPipeline, spark)
      }
      else {
        throw new IllegalArgumentException(s"Pipeline ${params.pipelineName} has not been constructed!!!")
      }
    } else if (Files.exists(Paths.get(params.initialPipeline))) {
      logger.info(s"loading pipeline from: ${params.initialPipeline}")
      if (params.pipelineName == "criteo") {
        CriteoPipeline.loadFromDisk(params.initialPipeline, spark)
      } else if (params.pipelineName == "url-rep") {
        URLRepPipeline.loadFromDisk(params.initialPipeline, spark)
      } else if (params.pipelineName == "taxi") {
        NYCTaxiPipeline.loadFromDisk(params.initialPipeline, spark)
      }
      else {
        throw new IllegalArgumentException(s"Pipeline ${params.pipelineName} has not been constructed!!!")
      }
    } else {
      val data = spark.textFile(params.inputPath)
      if (params.pipelineName == "criteo") {
        val pipeline = new CriteoPipeline(spark,
          stepSize = params.stepSize,
          numIterations = params.numIterations,
          regParam = params.regParam,
          convergenceTol = params.convergenceTol,
          miniBatchFraction = params.miniBatch,
          updater = params.updater,
          delim = params.delimiter,
          numCategories = params.numFeatures)
        logger.info(s"Training the pipeline: ${pipeline.toString}")
        pipeline.updateTransformTrain(data, params.numIterations)
        CriteoPipeline.saveToDisk(pipeline, params.initialPipeline)
        pipeline
      } else if (params.pipelineName == "url-rep") {
        val pipeline = new URLRepPipeline(spark,
          stepSize = params.stepSize,
          numIterations = params.numIterations,
          regParam = params.regParam,
          convergenceTol = params.convergenceTol,
          miniBatchFraction = params.miniBatch,
          updater = params.updater,
          numCategories = params.numFeatures)
        logger.info(s"Training the pipeline: ${pipeline.toString}")
        pipeline.updateTransformTrain(data, params.numIterations)
        URLRepPipeline.saveToDisk(pipeline, params.initialPipeline)
        pipeline
      } else if (params.pipelineName == "taxi") {
        val pipeline = new NYCTaxiPipeline(spark,
          stepSize = params.stepSize,
          numIterations = params.numIterations,
          regParam = params.regParam,
          convergenceTol = params.convergenceTol,
          miniBatchFraction = params.miniBatch,
          updater = params.updater)
        logger.info(s"Training the pipeline: ${pipeline.toString}")
        pipeline.updateTransformTrain(data, params.numIterations)
        NYCTaxiPipeline.saveToDisk(pipeline, params.initialPipeline)
        pipeline
      } else {
        throw new IllegalArgumentException(s"Pipeline ${params.pipelineName} has not been constructed!!!")
      }
    }
  }
}
