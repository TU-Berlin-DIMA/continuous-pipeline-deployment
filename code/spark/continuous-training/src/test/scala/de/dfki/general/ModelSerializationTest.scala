package de.dfki.general

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

    model.getUnderlyingModel.clearThreshold()
    val result = model.predictOnValues(test.map(lp => (lp.label, lp.features)))

    result.saveAsTextFile(s"$modelPath/$iters/result-predictions")

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
    val model = HybridModel.loadFromDisk(s"$modelPath/$iters/lr").asInstanceOf[HybridLR]
    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val vecParser = new CustomVectorParser()
    val test = sc.textFile(testPath).map(vecParser.parsePoint)

    val threshold = 0.9
    model.getUnderlyingModel.setThreshold(threshold)
    val result = model.predictOnValues(test.map(lp => (lp.label, lp.features)))
    val confusionMatrix = ConfusionMatrix.fromRDD(result)
    println(confusionMatrix.toString)

    //val metrics = new BinaryClassificationMetrics(result.map(r => (r._2, r._1)))

    //    println(s"area under ROC: ${metrics.areaUnderROC()}")
    //    metrics.precisionByThreshold().foreach(println)

    // println(result.toString)
  }
}

object LoadResults {
  val RESULT_PATH = "data/criteo-full/model/500/result-predictions"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val resultPath = parser.get("test-path", RESULT_PATH)

    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    def predictPoint(label: Double,
                     prediction: Double,
                     threshold: Double) = {
      if (prediction > threshold)
        (label, 1.0)
      else
        (label, 0.0)
    }
    sc.setLogLevel("WARN")
    for (threshold <- 0.1 to 0.9 by 0.1) {
      val labelAndPrediction = sc.textFile(resultPath).map(s => {
        val labelAndPrediction = s.replace("(", "").replace(")", "").split(",")
        (labelAndPrediction(0).toDouble, labelAndPrediction(1).toDouble)
      }).map(lp => predictPoint(lp._1, lp._2, threshold))



      val cMatrix = ConfusionMatrix.fromRDD(labelAndPrediction)
      println(s"threshold($threshold). confusion matrix: ${cMatrix.toFullString}")
    }
    //    val metrics = new BinaryClassificationMetrics(labelAndPrediction)
    //
    //    println(s"area under ROC: ${metrics.areaUnderROC()}")
    //    print(metrics.precisionByThreshold().max()(new Ordering[(Double, Double)] {
    //      override def compare(x: (Double, Double), y: (Double, Double)) = {
    //        Ordering[Double].compare(x._2, y._2)
    //      }
    //    }))

    //labelAndPrediction.map(lp => s"${lp._1},${lp._2}").repartition(1).saveAsTextFile(s"$resultPath/all-predictions/")
  }
}


