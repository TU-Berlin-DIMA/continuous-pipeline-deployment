package de.dfki.classification

import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}

import de.dfki.streaming.models.OnlineSVM
import de.dfki.utils.BatchFileInputDStream
import de.dfki.utils.MLUtils.{parsePoint, unparsePoint}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{ConstantInputDStream, DStream}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Base class for all SVM Classifier
  *
  * @author Behrouz Derakhshan
  */
abstract class SVMClassifier extends Serializable {


  // constants for the directory structures
  val DATA_DIRECTORY = "data/"
  val DATA_SET = "cover-types/"
  val BASE_DATA_DIRECTORY = DATA_DIRECTORY + DATA_SET
  val INITIAL_TRAINING = "initial-training/"
  val STREAM_TRAINING = "stream-training/"
  val TEST_DATA = "test/"


  // unique identifier for storing the error rates and historical data
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
  val tempDirectory = dateFormat.format(Calendar.getInstance().getTime)
  val historicalData = BASE_DATA_DIRECTORY + tempDirectory
  var streamingModel: OnlineSVM = _
  @transient var future: ScheduledFuture[_] = _
  @transient var execService: ScheduledExecutorService = _


  /**
    * Initialization of spark streaming context and checkpointing of stateful operators
    *
    * @return Spark Streaming Context object
    */
  def initializeSpark(masterURL: String = "spark://berlin-189.b.dfki.de:7077", batchDuration: Duration = Seconds(1)): StreamingContext = {
    val conf = new SparkConf().setMaster(masterURL).setAppName(getApplicationName())
    val ssc = new StreamingContext(conf, batchDuration)
    ssc.checkpoint("checkpoints/")
    ssc
  }

  /**
    * Process two streams of data.
    * Train the internal SVMModel using the observations DStream and write cumulative error rate to file system based
    * on the testData DStream
    *
    * @param testData     test Data DStream
    * @param observations observation data DStream
    * @param resultPath   directory for writing the error rate results
    */
  def streamProcessing(testData: DStream[LabeledPoint], observations: DStream[LabeledPoint], resultPath: String) {
    val storeErrorRate = (rdd: RDD[Double]) => {
      val file = s"${resultPath}/${DATA_SET}/error-rate-$tempDirectory.txt"
      val fw = new FileWriter(file, true)
      try {
        fw.write(rdd.collect().toList.mkString("\n") + "\n")
      }
      finally fw.close()
    }

    def mappingFunc(key: String, value: Option[(Double, Double)], state: State[(Double, Double)]): (Double, Double) = {
      val currentState = state.getOption().getOrElse(0.0, 0.0)
      val currentTuple = value.getOrElse(0.0, 0.0)
      val error = currentTuple._1 + currentState._1
      val sum = currentTuple._2 + currentState._2
      state.update(error, sum)
      (error, sum)
    }

    // periodically check test error
    streamingModel.predictOnValues(testData.map(lp => (lp.label, lp.features)))
      .map(a => {
        if (a._1 == a._2) {
          ("e", (0.0, 1.0))
        }
        else {
          ("e", (1.0, 1.0))
        }
      })
      .mapWithState(StateSpec.function(mappingFunc _))
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
      .map(item => (item._1 / item._2))
      .foreachRDD(storeErrorRate)

    val storeRDD = (rdd: RDD[String], time: Time) => {
      val hadoopConf = new Configuration()
      hadoopConf.set("mapreduce.output.basename", time.toString())
      rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$historicalData", classOf[NullWritable], classOf[String],
        classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
    }

    streamingModel.trainOn(observations)

    observations.map(unparsePoint).foreachRDD(storeRDD)
  }

  /**
    * Prequential Evaluation of the stream. First use the data to predict and train the model
    *
    * @param observations training data
    */
  def prequentialStreamEvaluation(observations: DStream[LabeledPoint], resultPath: String): Unit = {
    streamProcessing(observations, observations, resultPath)
  }

  def parseArgs(args: Array[String], baseDataDirectory: String): (String, String, String) = {

    if (args.length == 3) {
      // folder path for initial training data
      val initialDataPath = args(0)
      // folder path for data to be streamed
      val streamingDataPath = args(1)
      // folder (file) for test data
      val testDataPath = args(2)

      (initialDataPath, streamingDataPath, testDataPath)
    } else {
      (baseDataDirectory + INITIAL_TRAINING, baseDataDirectory + STREAM_TRAINING, baseDataDirectory + TEST_DATA)
    }
  }

  /**
    * Initialize an Online SVM model by first using the data in the given directory to train a static model
    * and then load the model into the Online SVM model
    *
    * @param ssc                    Spark Streaming Context
    * @param initialDataDirectories directory of initial data
    * @return Online SVM Model
    */
  def createInitialStreamingModel(ssc: StreamingContext, initialDataDirectories: String): OnlineSVM = {
    var model = trainModel(ssc.sparkContext, initialDataDirectories, 500)
    new OnlineSVM().setInitialModel(model) //.setNumIterations(1)
  }

  /**
    * Train a SVM Model from the data in the specified directories separated by comma
    *
    * @param sc            SparkContext object
    * @param trainingPath  list of directories separated by comma
    * @param numIterations num of iterations for the training process
    * @return SVMModel
    */
  def trainModel(sc: SparkContext, trainingPath: String, numIterations: Int = 10): SVMModel = {
    trainModel(sc.textFile(trainingPath).map(parsePoint).cache(), numIterations)
  }

  /**
    * Train a SVM Model from the RDD
    *
    * @param data          rdd
    * @param numIterations number of iterations for the training process
    * @return SVMModel
    */
  def trainModel(data: RDD[LabeledPoint], numIterations: Int): SVMModel = {
    SVMWithSGD.train(data, numIterations)
  }


  /**
    * Create a BatchFileInputDStream object from the given path
    *
    * @param ssc  Spark Streaming Context object
    * @param path input directory
    * @return BatchFileInputDStream object
    */
  def streamSource(ssc: StreamingContext, path: String): BatchFileInputDStream[LongWritable, Text, TextInputFormat] = {
    new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, path)
  }

  /**
    * Create a constant input DStream from the given path
    *
    * @param ssc  Spark Streaming Context object
    * @param path input directory
    * @return DStream object
    */
  def constantInputDStreaming(ssc: StreamingContext, path: String): DStream[LabeledPoint] = {
    val rdd = ssc.sparkContext.textFile(path).map(parsePoint)
    new ConstantInputDStream[LabeledPoint](ssc, rdd)
  }


  protected def createTempFolders(path: String): Unit = {
    val fs = FileSystem.get(new Configuration())
    fs.mkdirs(new Path(path))
  }

  def getApplicationName(): String

  def run(args: Array[String])

}
