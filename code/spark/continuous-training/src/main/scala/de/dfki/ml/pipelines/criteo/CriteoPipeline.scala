package de.dfki.ml.pipelines.criteo

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithRMSProp
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class CriteoPipeline(spark: SparkContext) {

  val fileReader = new InputParser()
  val missingValueImputer = new MissingValueImputer()
  val standardScaler = new StandardScaler()
  val model = new ModelTrainer(stepSize = 0.001,
    numIterations = 100,
    regParam = 0,
    miniBatchFraction = 1.0,
    updater = new SquaredL2UpdaterWithRMSProp())


  def nextTrainingBatch(data: RDD[String]): Unit = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    standardScaler.update(filledData)
    val scaledData = standardScaler.transform(spark, filledData)
    val trainingData = scaledData.map {
      d =>
        new LabeledPoint(d.label, Vectors.dense(d.numerical))
    }
    model.train(trainingData)
  }

  def predict(data: RDD[String]): RDD[(Double, Double)] = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.transform(spark, filledData)
    val testData = scaledData.map {
      d => (d.label, Vectors.dense(d.numerical))
    }
    model.predict(testData)
  }
}

object CriteoPipeline {
  val INPUT_PATH = "data/criteo-full/raw/0"
  val TEST_PATH = "data/criteo-full/raw/1"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)
    val testPath = parser.get("test-path", TEST_PATH)

    val conf = new SparkConf().setAppName("Criteo Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)

    val criteoPipeline = new CriteoPipeline(spark)
    val rawTraining = spark.textFile(inputPath)
    val rawTest = spark.textFile(testPath)

    criteoPipeline.nextTrainingBatch(rawTraining)

    val result = criteoPipeline.predict(rawTest)

    val loss = LogisticLoss.logisticLoss(result)

    println(s"Loss = $loss")
  }
}
