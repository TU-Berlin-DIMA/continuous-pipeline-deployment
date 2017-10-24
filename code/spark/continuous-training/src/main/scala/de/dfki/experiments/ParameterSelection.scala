package de.dfki.experiments

import java.io.{File, FileWriter}

import de.dfki.deployment.ContinuousDeployment
import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.AdvancedUpdaters
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object ParameterSelection {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val STREAM_PATH = "data/criteo-full/experiments/stream/1"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/loss"
  val UPDATER = "rmsprop"
  val DEFAULT_INCREMENT = "20,40,80,160,320,500"
  val DELIMITER = "\t"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val increments = parser.get("increments", DEFAULT_INCREMENT).split(",").map(_.toInt)
    val updater = AdvancedUpdaters.getUpdater(parser.get("updater", UPDATER))
    val delimiter = parser.get("delimiter", DELIMITER)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)
    val eval = ssc.sparkContext.textFile(evaluationPath)

    val criteoPipeline = new CriteoPipeline(ssc.sparkContext, delim = delimiter, updater = updater, miniBatchFraction = 0.1)
    criteoPipeline.update(data)
    var cur = 0
    increments.foreach { iter =>
      criteoPipeline.model.setNumIterations(iter - cur)
      criteoPipeline.train(data)
      val loss = LogisticLoss.logisticLoss(criteoPipeline.predict(eval))
      cur = iter
      val file = new File(s"$resultPath/${updater.name}")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        fw.write(s"$iter,$loss\n")
      }
      finally {
        fw.close()
      }
    }
    val deployment = new ContinuousDeployment(history = inputPath,
      stream = streamPath,
      eval = evaluationPath,
      resultPath = s"$resultPath/${updater.name}",
      slack = 1,
      samplingRate = 0.1)

    deployment.deploy(ssc, criteoPipeline)
  }


}
