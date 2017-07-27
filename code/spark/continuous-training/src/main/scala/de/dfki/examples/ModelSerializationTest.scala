package de.dfki.examples

import java.io.{File, PrintWriter}

import de.dfki.ml.evaluation.ConfusionMatrix
import de.dfki.ml.optimization.SquaredL2UpdaterWithMomentum
import de.dfki.ml.streaming.models.{HybridLR, HybridModel}
import de.dfki.preprocessing.parsers.CustomVectorParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object ModelSerializationTest {

  val TRAINING_DATA = "data/criteo-full/initial-training/0"
  val TEST_DATA = "data/criteo-full/processed/1"
  val MODEL_PATH = "data/criteo-full/model"
  val STEP_SIZE = 1.0
  val REGULARIZATION_PARAMETER = 0.1
  val ITERATIONS = 500
  val GAMMA = 0.9

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val trainingPath = parser.get("initial-training-path", TRAINING_DATA)
    val testPath = parser.get("test-path", TEST_DATA)
    val modelPath = parser.get("model-path", MODEL_PATH)

    val iters = parser.getInteger("num-iterations", ITERATIONS)
    val stepSize = parser.getDouble("step-size", STEP_SIZE)
    val regParam = parser.getDouble("reg-param", REGULARIZATION_PARAMETER)
    val updater = new SquaredL2UpdaterWithMomentum(parser.getDouble("gamma", GAMMA))


    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val vecParser = new CustomVectorParser()

    val training = sc.textFile(trainingPath).map(vecParser.parsePoint).cache()
    training.count()
    val test = sc.textFile(testPath).map(vecParser.parsePoint)

    val model = new HybridLR(stepSize = stepSize,
      numIterations = iters,
      regParam = regParam,
      miniBatchFraction = 1.0,
      updater = updater)
      .trainInitialModel(training)

    val result = model.predictOnValues(test.map(lp => (lp.label, lp.features))).map {
      v =>
        var tp, fp, tn, fn = 0
        if (v._1 == v._2 & v._1 == 1.0) tp = 1
        else if (v._1 == v._2 & v._1 == 0.0) tn = 1
        else if (v._1 != v._2 & v._1 == 1.0) fp = 1
        else fn = 1
        new ConfusionMatrix(tp, fp, tn, fn)
    }
      .reduce((c1, c2) => ConfusionMatrix.merge(c1, c2))

    val file = new File(s"$modelPath/$iters/confusion-matrix")
    file.getParentFile.mkdirs()
    file.createNewFile()

    new PrintWriter(file) {
      write(result.toString)
      close()
    }
    HybridModel.saveToDisk(s"$modelPath/$iters/lr", model)

  }
}

object LoadModel {
  val TEST_DATA = "data/criteo-full/processed/1"
  val MODEL_PATH = "data/criteo-full/model"
  val ITERATIONS = 500

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val testPath = parser.get("test-path", TEST_DATA)
    val modelPath = parser.get("model-path", MODEL_PATH)
    val iters = parser.getInteger("num-iterations", ITERATIONS)
    val model = HybridModel.loadFromDisk(s"$modelPath/$iters/lr")
    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val vecParser = new CustomVectorParser()
    val test = sc.textFile(testPath).map(vecParser.parsePoint)


    val result = model.predictOnValues(test.map(lp => (lp.label, lp.features))).map {
      v =>
        var tp, fp, tn, fn = 0
        if (v._1 == v._2 & v._1 == 1.0) tp = 1
        else if (v._1 == v._2 & v._1 == 0.0) tn = 1
        else if (v._1 != v._2 & v._1 == 1.0) fp = 1
        else fn = 1
        new ConfusionMatrix(tp, fp, tn, fn)
    }
      .reduce((c1, c2) => ConfusionMatrix.merge(c1, c2))

    println(result.toString)
  }
}
