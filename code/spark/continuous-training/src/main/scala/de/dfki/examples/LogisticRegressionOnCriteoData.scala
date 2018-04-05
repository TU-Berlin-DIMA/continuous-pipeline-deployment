package de.dfki.examples


import java.nio.file.{Files, Paths}

import de.dfki.ml.LinearAlgebra
import de.dfki.ml.classification.LogisticRegressionWithSGD
import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.updater._
import de.dfki.preprocessing.parsers.CustomVectorParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
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
  val TEST_DATA = "data/criteo-full/processed/1"
  val RESULT_PATH = "data/criteo-full/temp-results"
  val STEP_SIZE = "0.001"
  val REGULARIZATION_PARAMETER = "0.0"
  val ITERATIONS = "100"
  val OPTIMIZER = "sgd"
  val LEARNING_RATE = "l2-rmsprop"
  val MINI_BATCH_FRACTION = 1.0
  val DECAY_SIZE = 10
  val BETA1 = 0.9
  val BETA2 = 0.999

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val trainingPath = parser.get("initial-training-path", TRAINING_DATA)
    val testPath = parser.get("test-path", TEST_DATA)
    val resultPath = parser.get("result-path", RESULT_PATH)

    val iters = parser.get("num-iterations", ITERATIONS).split(",").map(_.trim.toInt)
    val steps = parser.get("step-size", STEP_SIZE).split(",").map(_.trim.toDouble)
    val regParams = parser.get("reg-param", REGULARIZATION_PARAMETER).split(",").map(_.trim.toDouble)
    val optimzers = parser.get("optimizer", OPTIMIZER).split(",").map(_.trim)
    val miniBatchFraction = parser.getDouble("mini-batch-fraction", MINI_BATCH_FRACTION)
    val updaters = parser.get("updater", LEARNING_RATE).split(",").map(_.trim).map {
      case "l2" => new SquaredL2Updater()
      case "l2-momentum" => new SquaredL2UpdaterWithMomentum()
      case "l2-step-decay" => new SquaredL2UpdaterWithStepDecay()
      case "l2-adadelta" => new SquaredL2UpdaterWithAdaDelta()
      case "l2-rmsprop" => new SquaredL2UpdaterWithRMSProp()
      case "l2-adam" => new SquaredL2UpdaterWithAdam()
      case "l2-constant" => new SquaredL2UpdaterWithConstantLearningRate()
      // dummy updater for LBFGS
      case _ => new NullUpdater()
    }

    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    val vecParser = new CustomVectorParser()

    val training = sc.textFile(trainingPath).map(vecParser.parsePoint).cache()
    training.count()
    val test = sc.textFile(testPath).map(vecParser.parsePoint)


    for (optimizer <- optimzers) {
      val finalUpdaters = optimizer match {
        case "lbfgs" =>
          println("Optimizer does not support learning rates, setting learning rate to null")
          List(new NullUpdater()).toArray
        case "sgd" => updaters
      }

      for (updater <- finalUpdaters) {
        for (it <- iters) {
          for (ss <- steps) {
            for (reg <- regParams) {


              val algorithm = new LogisticRegressionWithSGD(ss, it, reg, miniBatchFraction, updater)
              algorithm.optimizer.setConvergenceTol(0.0)
              val model = algorithm.run(training)
             // val weights = algorithm.optimizer.unStandardize(model.weights)
              val results = test.map { case LabeledPoint(label, features) =>
                val prediction = predictPoint(features, model.weights, model.intercept)
                (label, prediction)
              }
              val loss = LogisticLoss.fromRDD(results)
              println(s"loss = $loss")
              //              predictionAndLabels.repartition(8).saveAsTextFile(s"$resultPath/optimizer=$optimizer/updater=${updater.name}/iter=$it/step-size=$ss/reg=$reg")
              //              println(s"Execution with iter=$it\tstep-size=$ss\treg=$reg is completed")

            }

          }
        }
      }
    }
  }

  def predictPoint(dataMatrix: Vector,
                   weights: Vector,
                   intercept: Double) = {
    val margin = LinearAlgebra.dot(weights, dataMatrix) + intercept
    val score = 1.0 / (1.0 + math.exp(-margin))
    score
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

    val steps = List(0.01)
    //, 0.05, 0.1, 0.5, 1.0)
    val iters = List(500)
    //, 200, 300, 400, 500)
    val regParams = List(0.0)

    val optimizers = List("sgd")

    val updaters = List("l2-adam")
    //, 0.01, 0.05, 0.1, 0.5, 1.0)
    //, 0.01, 0.05, 0.1, 0.5, 1.0)
    // iterations, step-size, reg
    var results: List[(String, String, Int, Double, Double, Double)] = List()
    //    var maxAccuracy = ("", "", 0, 0.0, 0.0, -1.0)
    //    var maxPrecision = ("", "", 0, 0.0, 0.0, -1.0)
    //    var maxRecall = ("", "", 0, 0.0, 0.0, -1.0)
    //    var maxFMeasure = ("", "", 0, 0.0, 0.0, -1.0)


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
      val (finalUpdaters, finalRegPars) = if (opt == "lbfgs") {
        println("Optimizer does not support learning rates, setting learning rate to null")
        (List("null"), List(0.0))
      } else {
        (updaters, regParams)
      }
      for (updater <- finalUpdaters) {
        for (it <- iters) {
          for (ss <- steps) {
            for (reg <- finalRegPars) {
              val path = s"$resultPath/optimizer=$opt/updater=$updater/iter=$it/step-size=$ss/reg=$reg"
              if (Files.exists(Paths.get(path))) {
                val data = sc.textFile(s"$resultPath/optimizer=$opt/updater=$updater/iter=$it/step-size=$ss/reg=$reg").map(parse)
                val loss = LogisticLoss.fromRDD(data)
                println(s"loss = $loss")
                results = (opt, updater, it, ss, reg, loss.score()) :: results
              } else {
                println(s"directory: $path \n does not exist")

              }
            }
          }
        }
      }
    }

    for (r <- results) {
      println(s"optimizer(${r._1}), updater(${r._2}), iter(${r._3}), step(${r._4}), reg(${r._5}) -> ${r._6}")
    }

    //    println(s"Max Performance")
    //    println(s"accuracy(${maxAccuracy._6}) => optimizer(${maxAccuracy._1}), updater(${maxAccuracy._2}),iter(${maxAccuracy._3}), step(${maxAccuracy._4}), reg(${maxAccuracy._5})")
    //    println(s"precision(${maxPrecision._6}) => optimizer(${maxPrecision._1}), updater(${maxPrecision._2}),iter(${maxPrecision._3}), step(${maxPrecision._4}), reg(${maxPrecision._5})")
    //    println(s"recall(${maxRecall._6}) => optimizer(${maxRecall._1}), updater(${maxRecall._2}), iter(${maxRecall._3}), step(${maxRecall._4}), reg(${maxRecall._5})")
    //    println(s"f-measure(${maxFMeasure._6}) => optimizer(${maxFMeasure._1}), updater(${maxFMeasure._2}), iter(${maxFMeasure._3}), step(${maxFMeasure._4}), reg(${maxFMeasure._5})")
    //    //println(s"test-size($totalSize), class-dist(${classDist.toString})")

  }

  def parse(line: String): (Double, Double) = {
    val predict :: label :: other = line.split(",").map(s => s.replace("(", "").replace(")", "").toDouble).toList
    (predict, label)
  }
}
