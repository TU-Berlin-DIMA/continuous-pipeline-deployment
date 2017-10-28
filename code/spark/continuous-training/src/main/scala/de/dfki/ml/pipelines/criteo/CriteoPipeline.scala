package de.dfki.ml.pipelines.criteo

import java.io._

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.{AdvancedUpdaters, SquaredL2UpdaterWithAdam}
import de.dfki.ml.pipelines.Pipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class CriteoPipeline(@transient var spark: SparkContext,
                     val delim: String = "\t",
                     val stepSize: Double = 1.0,
                     val numIterations: Int = 500,
                     val regParam: Double = 0.0,
                     val miniBatchFraction: Double = 1.0,
                     val updater: AdvancedUpdaters = new SquaredL2UpdaterWithAdam(),
                     val numCategories: Int = 300000,
                     var materialization: Boolean = false) extends Pipeline {

  val fileReader = new InputParser(delim)
  val missingValueImputer = new MissingValueImputer()
  var standardScaler = new StandardScaler()
  val oneHotEncoder = new OneHotEncoder(numCategories)
  val model = new LRModel(stepSize, numIterations, regParam, miniBatchFraction, updater)


  override def setMaterialization(materialization: Boolean) = {
    this.materialization = materialization
  }

  /**
    * This method have to be called if the pipeline is loaded from the disk
    *
    * @param sc
    */
  def setSparkContext(sc: SparkContext): Unit = {
    this.spark = sc
  }

  override def update(data: RDD[String]): RDD[LabeledPoint] = {
    val parsedData = fileReader.transform(spark, data)
    val filledData = missingValueImputer.transform(spark, parsedData)
    val scaledData = standardScaler.updateAndTransform(spark, filledData)
    if (materialization) {
      // perform one hot encoding and return the materialized data
      oneHotEncoder.transform(spark, scaledData)
    } else {
      // do nothing
      null
    }
  }

  /**
    * train a new underlying model using the previous one as the starting point
    * user has to be make sure that the [[update]] method is called before the training
    * is done for every new batch of data
    *
    * @param data next batch of training data
    */
  override def train(data: RDD[String]) = {
    val trainingData = dataProcessing(data).cache()
    trainingData.count()
    model.train(trainingData)
  }

  /**
    * Train on already materialized data
    *
    * @param data materialized training data
    */
  override def trainOnMaterialized(data: RDD[LabeledPoint]) = {
    data.cache()
    data.count()
    model.train(data)
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


  /**
    * Create a fresh new pipeline using the same parameters
    *
    * @return
    */
  override def newPipeline() = {
    val newUpdater = AdvancedUpdaters.getUpdater(updater.name)
    new CriteoPipeline(spark = spark,
      delim = delim,
      stepSize = stepSize,
      numIterations = numIterations,
      regParam = regParam,
      miniBatchFraction = miniBatchFraction,
      updater = newUpdater,
      numCategories = numCategories)
  }

}

// example use case of criteo pipeline
object CriteoPipeline {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/day_0"
  val TEST_PATH = "data/criteo-full/raw/6"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input-path", INPUT_PATH)
    val testPath = parser.get("test-path", TEST_PATH)

    val conf = new SparkConf().setAppName("Criteo Pipeline Processing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)
    val criteoPipeline = new CriteoPipeline(spark, delim = ",", numIterations = 1)
    val rawTraining = spark.textFile("data/criteo-full/experiments/initial-training/0")
    criteoPipeline.update(rawTraining)
    criteoPipeline.train(rawTraining)

    CriteoPipeline.saveToDisk(criteoPipeline, "data/criteo-full/pipelines/test")

    val loadedPipeline = CriteoPipeline.loadFromDisk("data/criteo-full/pipelines/test", spark)

    val day1 = spark.textFile("data/criteo-full/experiments/stream/1")
    loadedPipeline.update(day1)
    loadedPipeline.train(day1)

    criteoPipeline.update(day1)
    criteoPipeline.train(day1)

    val rawTest = spark.textFile(testPath)

    val baseResult = criteoPipeline.predict(rawTest)
    val baseLoss = LogisticLoss.logisticLoss(baseResult)

    val loadedResult = criteoPipeline.predict(rawTest)
    val loadedLoss = LogisticLoss.logisticLoss(loadedResult)

    println(s"Base Loss = $baseLoss")
    println(s"Loaded Loss = $loadedLoss")
  }

  def saveToDisk(pipeline: CriteoPipeline, path: String): Unit = {
    val file = new File(path)
    file.getParentFile.mkdirs()
    file.createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(file, false))
    oos.writeObject(pipeline)
    oos.close()
  }

  def loadFromDisk(path: String, spark: SparkContext): CriteoPipeline = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    val pip = ois.readObject.asInstanceOf[CriteoPipeline]
    pip.setSparkContext(spark)
    ois.close()
    pip
  }
}



