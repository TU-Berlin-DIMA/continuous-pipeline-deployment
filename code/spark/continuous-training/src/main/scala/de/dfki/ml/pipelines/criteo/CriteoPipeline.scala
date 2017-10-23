package de.dfki.ml.pipelines.criteo

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithRMSProp
import de.dfki.ml.pipelines.Pipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class CriteoPipeline(spark: SparkContext,
                     withMaterialization: Boolean = false)
  extends Pipeline[String] {

  val fileReader = new InputParser()
  val missingValueImputer = new MissingValueImputer()
  val standardScaler = new StandardScaler()
  val oneHotEncoder = new OneHotEncoder()
  val model = new ModelTrainer(stepSize = 1.0,
    numIterations = 500,
    regParam = 0,
    miniBatchFraction = 1.0,
    updater = new SquaredL2UpdaterWithRMSProp())

  var materializedTrainingData: RDD[LabeledPoint] = _

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

  override def predict(data: RDD[String]): RDD[(Double, Double)] = {
    val testData = dataProcessing(data)
    model.predict(testData.map(v => (v.label, v.features)))
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

object Test {
  val INPUT_PATH = "data/criteo-full/raw/test"
  val TEST_PATH = "data/criteo-full/raw/6"

  def main(args: Array[String]): Unit = {


    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)

    val conf = new SparkConf().setAppName("Criteo Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)

    val inputParser = new InputParser()
    val standardScaler = new StandardScaler()
    val missingValueImputer = new MissingValueImputer()
    val oneHotEncoder = new OneHotEncoder()


    val data = spark.textFile(s"$inputPath")

    val parsedData = inputParser.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.updateAndTransform(spark, filledData)
    val trainingData = oneHotEncoder.updateAndTransform(spark, scaledData)

    trainingData.repartition(1).map(r => s"${r.label.toString} | ${r.features.toString}").saveAsTextFile(s"data/criteo-full/processed/new")


  }
}

