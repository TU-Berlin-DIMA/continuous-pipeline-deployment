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
  val VALIDATION_PATH = "data/criteo-full/evaluation-data"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/experiment-quality-0.1-l2-500.txt"
  val MODEL_PATH_ROOT = "data/criteo-full/models/daily/stochastic-0.1-l2-500"

  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)


    val batchRoot = parser.get("batch-path", BATCH_PATH_ROOT)
    val validationPath = parser.get("validation-path", VALIDATION_PATH)
    val modelPath = parser.get("model-path", MODEL_PATH_ROOT)
    val resultPath = parser.get("result-path", RESULT_PATH)


    val sc = new SparkContext(conf)
    val dataParser = new CustomVectorParser()

    // if model root path exists, perform evaluations
    if (Files.exists(Paths.get(modelPath))) {
      evaluateDailyModels(sc, List(0, 1, 2, 3, 4), modelPath, validationPath, resultPath, dataParser)
    } else {
      val startingDays: List[String] = List()
      trainDailyModels(sc, startingDays, List(0, 1, 2, 3, 4, 5), batchRoot, modelPath, dataParser)
    }
  }

  def trainDailyModels(sc: SparkContext,
                       startingDays: List[String],
                       days: List[Int],
                       batchRoot: String,
                       modelPath: String,
                       dataParser: DataParser) = {
    var inputs = startingDays
    for (i <- days) {
      inputs = s"$batchRoot/$i" :: inputs
      val trainingData = sc.textFile(inputs.mkString(",")).map(dataParser.parsePoint).repartition(sc.defaultParallelism).cache()
      trainingData.count()
      logger.info(s"training model for day $i")

      val model = new HybridLR()
        .setStepSize(0.001)
        .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
        .setMiniBatchFraction(0.1)
        .setRegParam(0.001)
        .setConvergenceTol(0.0)
        .setNumIterations(400)
      model.trainInitialModel(trainingData)
      trainingData.unpersist(true)
      HybridModel.saveToDisk(s"$modelPath/$i/model", model)
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
      val model = HybridModel.loadFromDisk(s"$modelPath/$i/model")
      val loss = LogisticLoss.logisticLoss(model.predictOnValues(evaluationDataSet))

      val file = new File(s"$resultPath")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        fw.write(s"$i,0.1,$loss\n")
      }
      finally fw.close()
    }

  }
}

object TrainLogisticRegressionModelOnCriteoData {
  val DATA_PATH = "data/criteo-full/all/0"
  val MODEL_PATH = "data/criteo-full/models/daily/0"


  def trainModel(sc: SparkContext, dataPath: String, dataParser: DataParser) = {
    val trainingData = sc.textFile(dataPath).map(dataParser.parsePoint).repartition(sc.defaultParallelism).cache()
    trainingData.count()
    new HybridLR()
      .setStepSize(0.001)
      .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
      .setMiniBatchFraction(0.1)
      .setRegParam(0.001)
      .setConvergenceTol(0.0)
      .setNumIterations(500)
      .trainInitialModel(trainingData)
  }

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    val dataParser = new CustomVectorParser()


    val dataPath = parser.get("batch-path", DATA_PATH)
    val modelPath = parser.get("model-path", MODEL_PATH)

    val model = trainModel(sc, dataPath, dataParser)
    HybridModel.saveToDisk(s"$modelPath", model)
  }
}

object CriteoModelEvaluation {
  val MODEL_PATH = "data/criteo-full/models/daily/stochastic-0.1-l2-500/0/model"
  val VALIDATION_PATH = "data/criteo-full/evaluation-data"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    val dataParser = new CustomVectorParser()


    val validationPath = parser.get("validation-path", VALIDATION_PATH)
    val modelPath = parser.get("model-path", MODEL_PATH)

    val model = HybridModel.loadFromDisk(modelPath)

    val evaluationDataSet = sc.textFile(validationPath)
      .map(dataParser.parsePoint)
      .map(lp => (lp.label, lp.features))
      .repartition(sc.defaultParallelism)
      .cache()

    val loss = LogisticLoss.logisticLoss(model.predictOnValues(evaluationDataSet))
    println(s"loss = $loss")


  }
}

object CriteoTrainingWithEvaluationData {
  val DATA_PATH = "data/criteo-full/all/0"
  val VALIDATION_INPUT = "data/criteo-full/evaluation-data"
  val RESULT_PATH = "../../../experiment-results/criteo-full/quality/training-loss.txt"
  val REG_PARAM = 0.001


  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    val dataParser = new CustomVectorParser()


    val dataPath = parser.get("batch-path", DATA_PATH)
    val validationPath = parser.get("validation-path", VALIDATION_INPUT)
    val trainingLossPath = parser.get("result-path", RESULT_PATH)
    val regParam = parser.getDouble("reg-param", REG_PARAM)

    val increments = 20
    val trainingData = sc.textFile(dataPath).map(dataParser.parsePoint).repartition(sc.defaultParallelism).cache()
    trainingData.count()

    val evaluationDataSet = sc.textFile(validationPath)
      .map(dataParser.parsePoint)
      .map(lp => (lp.label, lp.features))
      .repartition(sc.defaultParallelism)
      .cache()

    evaluationDataSet.count()

    val model = new HybridLR()
      .setStepSize(0.001)
      .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
      .setMiniBatchFraction(0.1)
      .setConvergenceTol(0.0)
      .setRegParam(regParam)
      .setNumIterations(increments)
      .trainInitialModel(trainingData)
    val file = new File(s"$trainingLossPath")
    file.getParentFile.mkdirs()

    var iter = increments
    while (iter < 400) {
      val loss = LogisticLoss.logisticLoss(model.predictOnValues(evaluationDataSet))
      val fw = new FileWriter(file, true)
      try {
        fw.write(s"$iter,$loss\n")
      }
      finally {
        fw.close()
      }
      model.trainOn(trainingData)
      iter += increments
    }
    val loss = LogisticLoss.logisticLoss(model.predictOnValues(evaluationDataSet))
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$iter,$loss\n")
    }
    finally {
      fw.close()
    }

  }

}

