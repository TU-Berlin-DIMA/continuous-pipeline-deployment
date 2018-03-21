package de.dfki.experiments

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import de.dfki.core.sampling.{RateBasedSampler, WindowBasedSampler}
import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.updater.Updater
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * TODO: FIX THIS
  *
  * @author behrouz
  */
object ParameterSelection {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val STREAM_PATH = "data/criteo-full/experiments/stream"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/loss-new"
  val PIPELINE_DIRECTORY = "data/criteo-full/pipelines/parameter-selection/"
  val UPDATER = "adam,rmsprop,momentum,adadelta"
  val DEFAULT_INCREMENT = "20,40,80,160,320,500"
  val DELIMITER = ","
  val NUM_FEATURES = 30000
  val SLACK = 10
  val DAYS = "1"
  val DAY_DURATION = 100
  val STEP_SIZE = 0.1


  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val increments = parser.get("increments", DEFAULT_INCREMENT).split(",").map(_.toInt)
    val updaters = parser.get("updater", UPDATER).split(",")
    val delimiter = parser.get("delimiter", DELIMITER)
    val stepSize = parser.getDouble("step", STEP_SIZE)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val pipelineDirectory = parser.get("pipeline", PIPELINE_DIRECTORY)
    val slack = parser.getInteger("slack", SLACK)
    val days = parser.get("days", DAYS).split(",").map(_.toInt)
    val dayDuration = parser.getInteger("day_duration", DAY_DURATION)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)
    val eval = ssc.sparkContext.textFile(evaluationPath)

    for (u <- updaters) {
      val updater = Updater.getUpdater(u)
      var criteoPipeline = new CriteoPipeline(ssc.sparkContext,
        stepSize = stepSize,
        delim = delimiter,
        updater = updater,
        miniBatchFraction = 0.1,
        numCategories = numFeatures)
      val transformed = criteoPipeline.updateAndTransform(data).setName("Transformed Training Data")
      transformed.cache()
      var cur = 0
      increments.foreach { iter =>
        val pipelineName = s"$pipelineDirectory/${updater.name}-$iter"
        if (Files.exists(Paths.get(pipelineName))) {
          logger.info(s"Pipeline for updater ${updater.name} and Iter $iter exists !!!")
          criteoPipeline = CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
        } else {
          criteoPipeline.model.setNumIterations(iter - cur)
          criteoPipeline.train(transformed)
          CriteoPipeline.saveToDisk(criteoPipeline, pipelineName)
        }
        val loss = LogisticLoss.fromRDD(criteoPipeline.predict(eval))
        cur = iter
        val file = new File(s"$resultPath/${updater.name}/loss")
        file.getParentFile.mkdirs()
        val fw = new FileWriter(file, true)
        try {
          fw.write(s"$iter,$loss\n")
        }
        finally {
          fw.close()
        }
      }

      val deployment = new ContinuousDeploymentQualityAnalysis(history = inputPath,
        streamBase = streamPath,
        evaluation = evaluationPath,
        resultPath = s"$resultPath/${updater.name}",
        daysToProcess = days,
        slack = slack,
        sampler = new WindowBasedSampler(dayDuration, dayDuration * 7))

      deployment.deploy(ssc, criteoPipeline)
    }
  }


}
