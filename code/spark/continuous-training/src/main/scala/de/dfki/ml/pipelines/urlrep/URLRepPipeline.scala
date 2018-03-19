package de.dfki.ml.pipelines.urlrep

import java.io._

import de.dfki.ml.evaluation.{ConfusionMatrix, Score}
import de.dfki.ml.optimization.updater.{SquaredL2UpdaterWithAdam, Updater}
import de.dfki.ml.pipelines.{ContinuousSVMModel, Pipeline}
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class URLRepPipeline(@transient var spark: SparkContext,
                     val stepSize: Double = 0.001,
                     val numIterations: Int = 500,
                     val regParam: Double = 0.0,
                     val miniBatchFraction: Double = 1.0,
                     val convergenceTol: Double = 1E-6,
                     val updater: Updater = new SquaredL2UpdaterWithAdam(),
                     val numCategories: Int = 300000) extends Pipeline {
  val fileReader = new URLRepSVMParser()
  val missingValueImputer = new URLRepMissingValueImputer()
  var standardScaler = new URLRepStandardScaler()
  val oneHotEncoder = new URLRepOneHotEncoder(numCategories)

  override val model = new ContinuousSVMModel(stepSize, numIterations, regParam, convergenceTol, miniBatchFraction, updater)

  /**
    * This method have to be called if the pipeline is loaded from the disk
    *
    * @param sc
    */
  def setSparkContext(sc: SparkContext): Unit = {
    this.spark = sc
  }

  override def update(data: RDD[String]) = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.update(spark, filledData)
    //oneHotEncoder.update(spark, scaledData)
  }

  override def transform(data: RDD[String]): RDD[LabeledPoint] = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.transform(spark, filledData)
    oneHotEncoder.transform(spark, scaledData)
  }

  override def updateAndTransform(data: RDD[String]): RDD[LabeledPoint] = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.updateAndTransform(spark, filledData)
    oneHotEncoder.transform(spark, scaledData)
  }

  /**
    * Train on already materialized data
    *
    * @param data materialized training data
    */
  override def train(data: RDD[LabeledPoint], iterations: Int = 1) = {
    model.setNumIterations(iterations)
    model.train(data)
    model.setNumIterations(1)
  }

  /**
    * short cut function for experiments for quality
    *
    * @param data
    */
  override def updateTransformTrain(data: RDD[String], iterations: Int = 1) = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.updateAndTransform(spark, filledData)
    val training = oneHotEncoder.transform(spark, scaledData)
    training.cache()
    training.count()
    model.setNumIterations(iterations)
    model.train(training)
    model.setNumIterations(1)
    training.unpersist()
  }

  /**
    *
    * @param data
    * @return
    */
  override def predict(data: RDD[String]): RDD[(Double, Double)] = {
    val testData = transform(data)
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


  /**
    * Create a fresh new pipeline using the same parameters
    *
    * @return
    */
  override def newPipeline() = {
    val newUpdater = Updater.getUpdater(updater.name)
    new URLRepPipeline(spark = spark,
      stepSize = stepSize,
      numIterations = numIterations,
      regParam = regParam,
      miniBatchFraction = miniBatchFraction,
      updater = newUpdater,
      numCategories = numCategories)
  }

  override def name() = "url-rep"

  override def score(data: RDD[String]): Score = {
    ConfusionMatrix.fromRDD(predict(data))
  }
}

object URLRepPipeline {
  val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
  val TEST_PATH = "data/url-reputation/processed/stream/day_1"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)
    val testPath = parser.get("test-path", TEST_PATH)

    val conf = new SparkConf().setAppName("URL Rep Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)
    val urlRepPipeline = new URLRepPipeline(spark,
      numIterations = 1000,
      stepSize = 0.001,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 1,
      regParam = 0.01,
      numCategories = 300000)
    val rawTraining = spark.textFile(inputPath)
    urlRepPipeline.updateTransformTrain(rawTraining)
    //urlRepPipeline.updateAndTransform(rawTraining).saveAsTextFile("data/url-reputation/processed/stream/day_1")
    URLRepPipeline.saveToDisk(urlRepPipeline, "data/url-reputation/pipelines/test")

    //val loadedPipeline = URLRepPipeline.loadFromDisk("data/url-reputation/pipelines/test", spark)

    //val day1 = spark.textFile("data/url-reputation/processed/stream/day_1")
    //loadedPipeline.updateTransformTrain(day1)

    //urlRepPipeline.updateTransformTrain(day1)

    val rawTest = spark.textFile(testPath)

    val baseResult = urlRepPipeline.predict(rawTest)
    val cMatrix = ConfusionMatrix.fromRDD(baseResult)

    //    val loadedResult = loadedPipeline.predict(rawTest)
    //    val loadedLoss = LogisticLoss.logisticLoss(loadedResult)
    //baseResult.saveAsTextFile("data/url-reputation/result/")
    println(s"Base Loss = $cMatrix")
    //println(s"Loaded Loss = $loadedLoss")
  }

  def saveToDisk(pipeline: URLRepPipeline, path: String): Unit = {
    val file = new File(path)
    file.getParentFile.mkdirs()
    file.createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(file, false))
    oos.writeObject(pipeline)
    oos.close()
  }

  def loadFromDisk(path: String, spark: SparkContext): URLRepPipeline = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    val pip = ois.readObject.asInstanceOf[URLRepPipeline]
    pip.setSparkContext(spark)
    ois.close()
    pip
  }
}
