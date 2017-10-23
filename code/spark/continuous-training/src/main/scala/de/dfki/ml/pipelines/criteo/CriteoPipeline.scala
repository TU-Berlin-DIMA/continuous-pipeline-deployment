package de.dfki.ml.pipelines.criteo

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.Pipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class CriteoPipeline(spark: SparkContext,
                     val stepSize: Double = 1.0,
                     val numIterations: Int = 200,
                     val regParam: Double = 0.0,
                     val miniBatchFraction: Double = 0.1,
                     val updater: Updater = new SquaredL2UpdaterWithAdam()) extends Pipeline[String] {

  val fileReader = new InputParser()
  val missingValueImputer = new MissingValueImputer()
  val standardScaler = new StandardScaler()
  val oneHotEncoder = new OneHotEncoder()
  val model = new ModelTrainer(stepSize, numIterations, regParam, miniBatchFraction, updater)

  var materializedTrainingData: RDD[LabeledPoint] = _

  override def withMaterialization = false

  override def update(data: RDD[String]): Unit = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.updateAndTransform(spark, filledData)
    if (withMaterialization) {
      materializeNextBatch(oneHotEncoder.updateAndTransform(spark, scaledData))
    } else {
      oneHotEncoder.update(scaledData)
    }
  }

  /**
    * train a new underlying model using the previous one as the starting point
    * user has to be make sure that the [[update]] method is called before the training
    * is done for every new batch of data
    *
    * @param data next batch of training data
    */
  override def train(data: RDD[String]): Unit = {
    // use the materialized data if the option is set
    val trainingData = if (withMaterialization) {
      materializedTrainingData
    } else {
      dataProcessing(data)
    }
    trainingData.cache()
    trainingData.count()
    model.train(trainingData)
    trainingData.unpersist()
  }

  /**
    *
    * @param data
    * @return
    */
  override def predict(data: RDD[String]): RDD[(Double, Double)] = {
    val testData = dataProcessing(data)
    model.predict(testData.map(v => (v.label, v.features)))
  }

  /**
    *
    * @param data
    * @return
    */
  override def predict(data: DStream[String]): DStream[(Double, Double)] = {
    data.transform(rdd => predict(rdd))
    //    val testData = dataProcessing(data)
    //    model.predict(testData.map(v => (v.label, v.features)))
  }


  private def dataProcessing(data: RDD[String]): RDD[LabeledPoint] = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.transform(spark, filledData)
    oneHotEncoder.transform(spark, scaledData)
  }


  private def materializeNextBatch(nextBatch: RDD[LabeledPoint]) = {
    if (materializedTrainingData == null) {
      materializedTrainingData = nextBatch
    } else {
      materializedTrainingData = materializedTrainingData.union(nextBatch)
    }
  }
}

// example use case of criteo pipeline
object CriteoPipeline {
  val INPUT_PATH = "data/criteo-full/raw"
  val TEST_PATH = "data/criteo-full/raw/6"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)
    val testPath = parser.get("test-path", TEST_PATH)

    val conf = new SparkConf().setAppName("Criteo Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)

    val criteoPipeline = new CriteoPipeline(spark)
    val directories = (0 to 0).map(i => s"$inputPath/$i")
    val rawTraining = spark.textFile(directories.toList.mkString(","))
    val rawTest = spark.textFile(testPath)

    criteoPipeline.update(rawTraining)
    criteoPipeline.train(rawTraining)

    val result = criteoPipeline.predict(rawTest)

    val loss = LogisticLoss.logisticLoss(result)

    println(s"Loss = $loss")
  }
}



