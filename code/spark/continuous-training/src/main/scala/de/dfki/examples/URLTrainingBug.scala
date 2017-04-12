package de.dfki.examples

import de.dfki.preprocessing.parsers.SVMParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object URLTrainingBug {
  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("URL Training Bug??")
    val sc = new SparkContext(conf)
    val masterURL = conf.get("spark.master", "local[*]")
    conf.set("spark.hadoop.validateOutputSpecs", "false")
    conf.setMaster(masterURL)

    val expType = parser.getInteger("type", 1)
    if (expType == 1)
      runModelWithCaching(sc, parser.get("initial-training-path"))
    if (expType == 2)
      runModel(sc, parser.get("initial-training-path"))
    if (expType == 3)
      runSimple(sc, parser.get("initial-training-path"))
  }

  def runSimple(sc: SparkContext, dataPath: String): Unit = {
    val parser = new SVMParser(3231961)


    var start = System.currentTimeMillis()
    sc.textFile(dataPath).map(parser.parsePoint).count()
    //val model = SVMWithSGD.train(data, numIterations, offlineStepSize, 0.01)
    var end = System.currentTimeMillis()
    println(s"Custom Parser: ${(end - start) / 1000.0}")

    start = System.currentTimeMillis()
    MLUtils.loadLibSVMFile(sc, dataPath, numFeatures = 3231961).count()
    end = System.currentTimeMillis()
    println(s"Spark SVM Loader: ${(end - start) / 1000.0}")

  }

  def runModel(sc: SparkContext, dataPath: String): Unit = {

    val parser = new SVMParser(3231961)

    val conf = new SparkConf().setMaster("local").setAppName("URL Training Bug??")
    val sc = new SparkContext(conf)
    var start = System.currentTimeMillis()
    val model1 = SVMWithSGD.train(sc.textFile(dataPath).map(parser.parsePoint), 500)
    var end = System.currentTimeMillis()
    println(s"Custom Parser: ${(end - start) / 1000.0}")

    start = System.currentTimeMillis()
    val model2 = SVMWithSGD.train(MLUtils.loadLibSVMFile(sc, dataPath, numFeatures = 3231961), 500)
    end = System.currentTimeMillis()
    println(s"Spark SVM Loader: ${(end - start) / 1000.0}")
  }

  def runModelWithCaching(sc: SparkContext, dataPath: String): Unit = {
    val parser = new SVMParser(3231961)

    val conf = new SparkConf().setMaster("spark://berlin-241.b.dfki.de:7077").setAppName("URL Training Bug??")
    val sc = new SparkContext(conf)
    var start = System.currentTimeMillis()
    val cachedData = sc.textFile(dataPath).map(parser.parsePoint).cache()
    cachedData.count()
    val model1 = SVMWithSGD.train(cachedData, 500)
    cachedData.unpersist(blocking = false)
    var end = System.currentTimeMillis()
    println(s"Custom Parser: ${(end - start) / 1000.0}")


    start = System.currentTimeMillis()
    val cachedDataSpark = MLUtils.loadLibSVMFile(sc, dataPath, numFeatures = 3231961).cache()
    cachedDataSpark.count()
    val model2 = SVMWithSGD.train(cachedDataSpark, 500)
    cachedDataSpark.unpersist(blocking = false)
    end = System.currentTimeMillis()
    println(s"Spark SVM Loader: ${(end - start) / 1000.0}")
  }

}
