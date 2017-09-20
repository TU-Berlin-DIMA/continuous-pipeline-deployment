package de.dfki.experiments

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.streaming.models.{HybridLR, HybridModel}
import de.dfki.preprocessing.parsers.{CustomVectorParser, DataParser}
import de.dfki.utils.CommandLineParser
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object Experiment_Quality {
  val BATCH_PATH_ROOT = "data/criteo-full/all"
  val VALIDATION_INPUT = "data/criteo-full/validation"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/experiment-quality.txt"
  val MODEL_PATH_ROOT = "data/criteo-full/models"
  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)


    val batchRoot = parser.get("batch-path", BATCH_PATH_ROOT)
    val validationPath = parser.get("validation-path", VALIDATION_INPUT)
    val modelPath = parser.get("model-path", MODEL_PATH_ROOT)
    val resultPath = parser.get("result-path", RESULT_PATH)

    //val resultPath = parser.get("result-path", RESULT_PATH)

    val sc = new SparkContext(conf)
    val dataParser = new CustomVectorParser()


    // if model root path exists, perform evaluations
    if (Files.exists(Paths.get(modelPath))) {
      evaluateDailyModels(sc, (0 to 5).toList, modelPath, validationPath, resultPath, dataParser)
    } else {
      val startingDays: List[String] = List()
      trainDailyModels(sc, startingDays, (0 to 5).toList, batchRoot, modelPath, dataParser)
    }
  }

  def trainDailyModels(sc: SparkContext,
                       existingDays: List[String],
                       days: List[Int],
                       batchRoot: String,
                       modelPath: String,
                       dataParser: DataParser) = {
    var inputs = existingDays
    for (i <- days) {
      inputs = s"$batchRoot/$i" :: inputs
      val trainingData = sc.textFile(existingDays.mkString(",")).map(dataParser.parsePoint).cache()
      val model_10 = new HybridLR()
        .setStepSize(0.001)
        .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
        .setMiniBatchFraction(1.0)
        .setNumIterations(500)
        .trainInitialModel(trainingData)

      val model_02 = new HybridLR()
        .setStepSize(0.001)
        .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
        .setMiniBatchFraction(0.2)
        .setNumIterations(500)
        .trainInitialModel(trainingData)

      trainingData.unpersist(true)
      HybridModel.saveToDisk(s"$modelPath/$i/model_10", model_10)
      HybridModel.saveToDisk(s"$modelPath/$i/model_02", model_02)
    }
  }

  def evaluateDailyModels(sc: SparkContext,
                          days: List[Int],
                          modelPath: String,
                          validationPath: String,
                          resultPath: String,
                          dataParser: DataParser) = {
    val evaluationDataSet = sc.textFile(validationPath)
      .map(dataParser.parsePoint)
      .map(lp => (lp.label, lp.features))
    for (i <- days) {
      val model10 = HybridModel.loadFromDisk(s"$modelPath/$i/model_10")
      val model02 = HybridModel.loadFromDisk(s"$modelPath/$i/model_02")
      val loss10 = LogisticLoss.logisticLoss(model10.predictOnValues(evaluationDataSet))
      val loss02 = LogisticLoss.logisticLoss(model02.predictOnValues(evaluationDataSet))

      val file = new File(s"$resultPath")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        fw.write(s"$i,1.0,$loss10\n")
        fw.write(s"$i,0.2,$loss02\n")
      }
      finally fw.close()
    }

  }


}

