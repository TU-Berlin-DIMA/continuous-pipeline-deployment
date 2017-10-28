package de.dfki.experiments

import java.io.{File, FileWriter}
import java.nio.file.{Files, Path, Paths}

import de.dfki.deployment.ContinuousDeploymentNoOptimization
import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.AdvancedUpdaters
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * TODO: FIX THIS
  * @author behrouz
  */
object ParameterSelection {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val STREAM_PATH = "data/criteo-full/experiments/stream/1"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/loss-new"
  val PIPELINE_DIRECTORY = "data/criteo-full/pipelines/parameter-selection/"
  val UPDATER = "rmsprop"
  val DEFAULT_INCREMENT = "20,40,80,160,320,500"
  val DELIMITER = ","
  val NUM_FEATURES = 3000000
  val SLACK = 10

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val increments = parser.get("increments", DEFAULT_INCREMENT).split(",").map(_.toInt)
    val updater = AdvancedUpdaters.getUpdater(parser.get("updater", UPDATER))
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val pipelineDirectory = parser.get("pipeline", PIPELINE_DIRECTORY)
    val slack = parser.getInteger("slack", SLACK)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)
    val eval = ssc.sparkContext.textFile(evaluationPath)

    var criteoPipeline = new CriteoPipeline(ssc.sparkContext,
      delim = delimiter,
      updater = updater,
      miniBatchFraction = 0.1,
      numCategories = numFeatures)
    criteoPipeline.update(data)
    var cur = 0
    increments.foreach { iter =>
      val pipelineName = s"$pipelineDirectory/${updater.name}-$iter"
      if (Files.exists(Paths.get(pipelineName))) {
        logger.info(s"Pipeline for updater ${updater.name} and Iter $iter exists !!!")
        criteoPipeline = CriteoPipeline.loadFromDisk(pipelineName, ssc.sparkContext)
      } else {
        criteoPipeline.model.setNumIterations(iter - cur)
        criteoPipeline.train(data)
        CriteoPipeline.saveToDisk(criteoPipeline, pipelineName)
      }
      val loss = LogisticLoss.logisticLoss(criteoPipeline.predict(eval))
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
    val deployment = new ContinuousDeploymentNoOptimization(history = inputPath,
      stream = streamPath,
      resultPath = s"$resultPath/${updater.name}",
      slack = slack,
      samplingRate = 0.1)

    deployment.deploy(ssc, criteoPipeline)
  }


}
