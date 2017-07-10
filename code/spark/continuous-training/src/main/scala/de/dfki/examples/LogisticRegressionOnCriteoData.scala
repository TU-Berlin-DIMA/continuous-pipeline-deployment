package de.dfki.examples


import de.dfki.ml.classification.LogisticRegressionWithSGD
import de.dfki.ml.evaluation.ConfusionMatrix
import de.dfki.preprocessing.parsers.CustomVectorParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * ~/Documents/frameworks/spark-2.1.0-bin-hadoop2.7/bin/spark-submit \
  * --class de.dfki.examples.LogisticRegressionOnCriteoData \
  * --master "spark://berlin-235.b.dfki.de:7077"  --executor-memory 5G \
  * /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
  * "initial-training-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/initial-training/0" \
  * "test-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/streaming-day-based/1" \
  * "result-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/temp-results"
  *
  * Best hyperparameters from grid search:
  * iter(100), step(0.5), reg(1.0) -> error(0.03358706662253914)
  *
  *
  * accuracy(0.9860693729944481) => iter(100), step(1.0), reg(0.0)
  * precision(0.9471678943357886) => iter(100), step(1.0), reg(0.0)
  * recall(0.5960677749360613) => iter(100), step(1.0), reg(0.0)
  * f-measure(0.7316786029628176) => iter(100), step(1.0), reg(0.0)
  *
  * @author behrouz
  */
object LogisticRegressionOnCriteoData {

  val TRAINING_DATA = "data/criteo-full/initial-training/0"
  val TEST_DATA = "data/criteo-full/streaming-day-based/1"
  val RESULT_PATH = "data/criteo-full/temp-results"
  val STEP_SIZE = "1.0"
  val REGULARIZATION_PARAMETER = "0.01,0.5,0.1"
  val ITERATIONS = "100"
  val OPTIMIZER = "sgd"

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val trainingPath = parser.get("initial-training-path", TRAINING_DATA)
    val testPath = parser.get("test-path", TRAINING_DATA)
    val resultPath = parser.get("result-path", RESULT_PATH)

    val iters = parser.get("num-iterations", ITERATIONS).split(",").map(_.trim.toInt)
    val steps = parser.get("step-size", STEP_SIZE).split(",").map(_.trim.toDouble)
    val regParams = parser.get("reg-param", REGULARIZATION_PARAMETER).split(",").map(_.trim.toDouble)
    val optimzers = parser.get("optimizer", OPTIMIZER).split(",").map(_.trim)

    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val vecParser = new CustomVectorParser()

    val training = sc.textFile(trainingPath).map(vecParser.parsePoint).cache()
    training.count()
    val test = sc.textFile(testPath).map(vecParser.parsePoint)


    for (optimizer <- optimzers) {
      for (it <- iters) {
        for (ss <- steps) {
          for (reg <- regParams) {
            val algorithm =
              if (optimizer == "sgd")
                new LogisticRegressionWithSGD(ss, it, reg, 1.0)
              else {
                val alg = new LogisticRegressionWithLBFGS()
                alg.optimizer.setNumIterations(it)
                alg.optimizer.setRegParam(ss)
                alg
              }
            val model = algorithm.run(training)
            //val model = new LogisticRegressionWithSGD(ss, it, reg, 1.0).run(training)
            val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
              val prediction = model.predict(features)
              (prediction, label)
            }
            predictionAndLabels.repartition(8).saveAsTextFile(s"$resultPath/optimizer=$optimizer/iter=$it/step-size=$ss/reg=$reg")
            println(s"Execution with iter=$it\tstep-size=$ss\treg=$reg is completed")
          }
        }
      }
    }

  }

}

object ComputeScores {
  val RESULT_PATH = "data/criteo-full/temp-results"

  case class Metrics(precision: Double, recall: Double, f1: Double)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val resultPath = parser.get("result-path", RESULT_PATH)
    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    val steps = List(1.0)
    //, 0.05, 0.1, 0.5, 1.0)
    val iters = List(100)
    //, 200, 300, 400, 500)
    val regParams = List(0.01, 0.5, 0.1)

    val optimizers = List("sgd")
    //, 0.01, 0.05, 0.1, 0.5, 1.0)
    //, 0.01, 0.05, 0.1, 0.5, 1.0)
    // iterations, step-size, reg
    var results: List[(Int, Double, Double, ConfusionMatrix)] = List()
    var maxAccuracy = (0, 0.0, 0.0, -1.0)
    var maxPrecision = (0, 0.0, 0.0, -1.0)
    var maxRecall = (0, 0.0, 0.0, -1.0)
    var maxFMeasure = (0, 0.0, 0.0, -1.0)


    //    val data = sc.textFile(s"$resultPath/optimizer=sgd/iter=10000/step-size=1.0/reg=0.0")
    //    val totalSize = data.count()
    //    val classDist = data
    //      .map(parse)
    //      .map(_._2)
    //      .groupBy(a => a)
    //      .map(a => (a._1, a._2.toList.size))
    //      .collect()
    //      .toList
    for (opt <- optimizers) {
      for (it <- iters) {
        for (ss <- steps) {
          for (reg <- regParams) {
            val data = sc.textFile(s"$resultPath/optimizer=$opt/iter=$it/step-size=$ss/reg=$reg").map(parse)
            val cMatrix = createConfusionMatrix(data)
            if (maxAccuracy._4 < cMatrix.accuracy) {
              maxAccuracy = (it, ss, reg, cMatrix.accuracy)
            }
            if (maxPrecision._4 < cMatrix.precision) {
              maxPrecision = (it, ss, reg, cMatrix.precision)
            }
            if (maxRecall._4 < cMatrix.recall) {
              maxRecall = (it, ss, reg, cMatrix.recall)
            }
            if (maxFMeasure._4 < cMatrix.fMeasure) {
              maxFMeasure = (it, ss, reg, cMatrix.fMeasure)
            }

            results = (it, ss, reg, cMatrix) :: results

          }
        }
      }
    }

    for (r <- results) {
      println(s"iter(${r._1}), step(${r._2}), reg(${r._3}) -> ${r._4.toString}")
    }

    println(s"Max Performance")
    println(s"accuracy(${maxAccuracy._4}) => iter(${maxAccuracy._1}), step(${maxAccuracy._2}), reg(${maxAccuracy._3})")
    println(s"precision(${maxPrecision._4}) => iter(${maxPrecision._1}), step(${maxPrecision._2}), reg(${maxPrecision._3})")
    println(s"recall(${maxRecall._4}) => iter(${maxRecall._1}), step(${maxRecall._2}), reg(${maxRecall._3})")
    println(s"f-measure(${maxFMeasure._4}) => iter(${maxFMeasure._1}), step(${maxFMeasure._2}), reg(${maxFMeasure._3})")
    //println(s"test-size($totalSize), class-dist(${classDist.toString})")

  }

  def accuracy(data: RDD[(Double, Double)]): Double = {
    accuracy(data.collect)
  }

  def accuracy(data: Array[(Double, Double)]): Double = {
    val size = data.length
    data.
      map(v => if (v._1 == v._2) 1 else 0).
      sum.toDouble / size.toDouble
  }

  def createConfusionMatrix(data: RDD[(Double, Double)]): ConfusionMatrix = {
    createConfusionMatrix(data.collect)
  }

  def createConfusionMatrix(data: Array[(Double, Double)]): ConfusionMatrix = {
    var tp, tn, fp, fn = 0
    data.
      foreach {
        v =>
          if (v._1 == v._2 & v._1 == 1.0) tp += 1
          else if (v._1 == v._2 & v._1 == 0.0) tn += 1
          else if (v._1 != v._2 & v._1 == 1.0) fp += 1
          else fn += 1
      }
    new ConfusionMatrix(tp, fp, tn, fn)

  }

  def parse(line: String): (Double, Double) = {
    val predict :: label :: other = line.split(",").map(s => s.replace("(", "").replace(")", "").toDouble).toList
    (predict, label)
  }
}
