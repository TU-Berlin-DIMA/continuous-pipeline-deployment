package de.dfki.ml.pipelines.nyc_taxi

import java.io._

import de.dfki.ml.evaluation.RMSLE
import de.dfki.ml.optimization.updater.{SquaredL2UpdaterWithAdam, Updater}
import de.dfki.ml.pipelines.{ContinuousLinearRegressionModel, Pipeline}
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class NYCTaxiPipeline(@transient var spark: SparkContext,
                      val stepSize: Double = 0.001,
                      val numIterations: Int = 500,
                      val regParam: Double = 0.0,
                      val convergenceTol: Double = 1E-6,
                      val miniBatchFraction: Double = 1.0,
                      val updater: Updater = new SquaredL2UpdaterWithAdam()) extends Pipeline {
  val fileReader = new NYCInputParser()
  val featureExtractor = new NYCFeatureExtractor()
  val anomalyDetector = new NYCAnomalyDetector()
  val standardScaler = new NYCStandardScaler()
  override val model = new ContinuousLinearRegressionModel(stepSize = stepSize,
    numIterations = numIterations,
    regParam = regParam,
    convergenceTol = convergenceTol,
    miniBatchFraction = miniBatchFraction,
    updater = updater)


  override def setSparkContext(sc: SparkContext) = {
    this.spark = sc
  }

  override def predict(data: RDD[String]): RDD[(Double, Double)] = {
    val testData = transform(data)
    model.predict(testData.map(v => (v.label, v.features)))
  }

  override def predict(data: DStream[String]): DStream[(Double, Double)] = {
    data.transform(rdd => predict(rdd))
  }

  override def score(data: RDD[String]) = {
    RMSLE.fromRDD(predict(data))
  }

  override def update(data: RDD[String]) = {
    val parsed = fileReader.updateAndTransform(spark, data)
    val features = featureExtractor.updateAndTransform(spark, parsed)
    val cleaned = anomalyDetector.updateAndTransform(spark, features)
    standardScaler.update(spark, cleaned)
  }

  override def transform(data: RDD[String]): RDD[LabeledPoint] = {
    val parsed = fileReader.transform(spark, data)
    val features = featureExtractor.transform(spark, parsed)
    val cleaned = anomalyDetector.transform(spark, features)
    standardScaler.transform(spark, cleaned)
  }

  override def train(data: RDD[LabeledPoint], iterations: Int) = {
    val currentIter = model.getNumIterations
    model.setNumIterations(iterations)
    model.train(data)
    model.setNumIterations(currentIter)
  }

  override def updateAndTransform(data: RDD[String]): RDD[LabeledPoint] = {
    val parsed = fileReader.updateAndTransform(spark, data)
    val features = featureExtractor.updateAndTransform(spark, parsed)
    val cleaned = anomalyDetector.updateAndTransform(spark, features)
    standardScaler.updateAndTransform(spark, cleaned)
  }

  override def updateTransformTrain(data: RDD[String], iterations: Int) = {
    val parsed = fileReader.updateAndTransform(spark, data)
    val features = featureExtractor.updateAndTransform(spark, parsed)
    val cleaned = anomalyDetector.updateAndTransform(spark, features)
    val training = standardScaler.updateAndTransform(spark, cleaned)
    training.cache()
    training.count()
    val currentIter = model.getNumIterations
    model.setNumIterations(iterations)
    model.train(training)
    model.setNumIterations(currentIter)
    training.unpersist()
  }


  override def newPipeline() = {
    val newUpdater = Updater.getUpdater(updater.name)
    new NYCTaxiPipeline(spark = spark,
      stepSize = stepSize,
      numIterations = numIterations,
      regParam = regParam,
      convergenceTol = convergenceTol,
      miniBatchFraction = miniBatchFraction,
      updater = newUpdater)
  }

  override def name() = "nyc_taxi"
}

object NYCTaxiPipeline {
  val INPUT_PATH = "data/nyc-taxi/raw/yellow_tripdata_2015-01.csv"
  val TEST_PATH = "data/nyc-taxi/raw/yellow_tripdata_2015-02.csv"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)
    val testPath = parser.get("test-path", TEST_PATH)

    val conf = new SparkConf().setAppName("URL Rep Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val spark = new SparkContext(conf)

    val nycTaxiPipeline = new NYCTaxiPipeline(spark = spark,
      stepSize = 0.01,
      numIterations = 10000,
      regParam = 0.001,
      convergenceTol = 1E-9,
      miniBatchFraction = 1,
      updater = new SquaredL2UpdaterWithAdam())

    val rawTraining = spark.textFile(inputPath)
    nycTaxiPipeline.updateTransformTrain(rawTraining, 2000)
    //saveToDisk(nycTaxiPipeline, "data/nyc-taxi/pipelines/test")
   // val nycTaxiPipeline = loadFromDisk("data/nyc-taxi/pipelines/test", spark)

    val rawTest = spark.textFile(testPath)

    val rmsle = nycTaxiPipeline.score(rawTest)
    println(s"rmsle = ${rmsle.score()}")

  }

  def saveToDisk(pipeline: NYCTaxiPipeline, path: String): Unit = {
    val file = new File(path)
    file.getParentFile.mkdirs()
    file.createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(file, false))
    oos.writeObject(pipeline)
    oos.close()
  }

  def loadFromDisk(path: String, spark: SparkContext): NYCTaxiPipeline = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    val pip = ois.readObject.asInstanceOf[NYCTaxiPipeline]
    pip.setSparkContext(spark)
    ois.close()
    pip
  }
}
