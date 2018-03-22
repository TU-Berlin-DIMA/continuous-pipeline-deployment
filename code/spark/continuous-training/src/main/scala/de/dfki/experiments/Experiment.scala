package de.dfki.experiments

import java.nio.file.{Files, Paths}

import de.dfki.experiments.profiles.Profile
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.urlrep.URLRepPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class Experiment {
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
      regParam = regParam)
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
