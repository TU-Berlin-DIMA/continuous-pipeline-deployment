package de.dfki.classification

import java.io.{File, FileWriter}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}

import de.dfki.preprocessing.parsers.{CSVParser, DataParser, SVMParser}
import de.dfki.streaming.models.OnlineSVM
import de.dfki.utils.{BatchFileInputDStream, CommandLineParser}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.log4j.Logger
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{ConstantInputDStream, DStream}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Base class for all SVM Classifier
  *
  * The argument to the main class should be a set of key, value pairs in the format of key=value
  *
  * Common arguments:
  * batch-duration : spark's batch duration (default 1 seconds)
  * result-path: root directory for writing experiment results
  * initial-training-path: data used for initial training
  * streaming-path: data used for online training (and prequential evaluation)
  * test-path: data used for evaluation (if not specified, prequential evaluation is used)
  *
  * [[ContinuousClassifier]] and [[VeloxClassifier]] require extra arguments
  *
  * @author Behrouz Derakhshan
  */
abstract class SVMClassifier extends Serializable {

  @transient var future: ScheduledFuture[_] = _
  @transient var execService: ScheduledExecutorService = _
  @transient val logger = Logger.getLogger(getClass.getName)

  // time captured at the beginning of the experiments. Used for generating unique ids
  private val experimentTime = Calendar.getInstance().getTime

  // constants for the directory structures
  val DATA_DIRECTORY = "data"
  val DATA_SET = "criteo-sample/processed"
  val BASE_DATA_DIRECTORY: String = s"$DATA_DIRECTORY/$DATA_SET"
  val INITIAL_TRAINING = "initial-training"
  val STREAM_TRAINING = "stream-training"
  val TEST_DATA = "test"

  var streamingModel: OnlineSVM = _
  var dataParser: DataParser = _

  def experimentResultPath(root: String, parent: String): String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val experimentId = dateFormat.format(experimentTime)
    s"$root/$parent/$experimentId"
  }

  var numIterations: Int = _
  var errorType: String = _
  var batchDuration: Long = _
  var offlineStepSize: Double = _
  var onlineStepSize: Double = _
  var defaultParallelism: Int = _


  def parseArgs(args: Array[String]): (String, String, String, String) = {
    val parser = new CommandLineParser(args).parse()
    // spark streaming batch duration
    batchDuration = parser.getLong("batch-duration", defaultBatchDuration)
    // path for storing experiments results
    val resultRoot = parser.get("result-path", s"results/$DATA_SET/$getExperimentName")
    // folder path for initial training data
    val initialDataPath = parser.get("initial-training-path", s"$BASE_DATA_DIRECTORY/$INITIAL_TRAINING")
    // folder path for data to be streamed
    val streamingDataPath = parser.get("streaming-path", s"$BASE_DATA_DIRECTORY/$STREAM_TRAINING")
    // folder (file) for test data
    val testDataPath = parser.get("test-path", "prequential")
    // cumulative test error
    errorType = parser.get("error-type", "cumulative")
    // number of iterations
    numIterations = parser.getInteger("num-iterations", 500)
    // offline learner step size
    offlineStepSize = parser.getDouble("offline-step-size", 1.0)
    // online learner step size
    onlineStepSize = parser.getDouble("online-step-size", 1.0)


    if (parser.get("input-format", "text") == "text") {
      dataParser = new CSVParser()
    } else {
      dataParser = new SVMParser(parser.getInteger("feature-size"))
    }

    (resultRoot, initialDataPath, streamingDataPath, testDataPath)
  }

  /**
    * Initialization of spark streaming context and checkpointing of stateful operators
    *
    * @return Spark Streaming Context object
    */
  def initializeSpark(): StreamingContext = {
    val conf = new SparkConf().setAppName(getApplicationName)
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val ssc = new StreamingContext(conf, Seconds(batchDuration))
    defaultParallelism = ssc.sparkContext.defaultParallelism
    ssc.checkpoint("checkpoints/")
    ssc
  }

  /**
    * Store the cumulative error rate of the incoming stream in the given result directory
    *
    * @param testData test Data DStream
    */
  def evaluateStream(testData: DStream[LabeledPoint], resultPath: String) {

    // periodically check test error
    val predictions = streamingModel.predictOnValues(testData.map(lp => (lp.label, lp.features)))
      .map(a => {
        if (a._1 == a._2) {
          (0.0, 1.0)
        }
        else {
          (1.0, 1.0)
        }
      })

    if (errorType == "cumulative") {
      predictions
        .map(p => ("e", p))
        .mapWithState(StateSpec.function(mappingFunc _))
        .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
        .map(item => item._1 / item._2)
        .foreachRDD(rdd => storeErrorRate(rdd, resultPath))
    } else {
      predictions
        .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
        .map(item => item._1 / item._2)
        .foreachRDD(rdd => storeErrorRate(rdd, resultPath))
    }

  }


  private val storeErrorRate = (rdd: RDD[Double], resultPath: String) => {
    val file = new File(s"$resultPath/error-rates.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      val content = rdd.collect().toList.mkString("\n")
      if (content == "") {}
      else {
        fw.write(s"$content\n")
      }
    }
    finally fw.close()
  }

  private def mappingFunc(key: String, value: Option[(Double, Double)], state: State[(Double, Double)]): (Double, Double) = {
    val currentState = state.getOption().getOrElse(0.0, 0.0)
    val currentTuple = value.getOrElse(0.0, 0.0)
    val error = currentTuple._1 + currentState._1
    val sum = currentTuple._2 + currentState._2
    println(s"New State: ($error : $sum)")
    state.update(error, sum)
    (error, sum)
  }

  /**
    *
    * incrementally update the [[streamingModel]] using the incoming data stream
    *
    * @param observations training data stream of [[LabeledPoint]]
    *
    */
  def trainOnStream(observations: DStream[LabeledPoint]): Unit = {
    streamingModel.trainOn(observations)
  }

  /**
    * Write content of the DStream to the specified location
    * This is used for further retraining
    *
    * @param stream input stream
    * @param path   output location
    */
  def writeStreamToDisk(stream: DStream[String], path: String): Unit = {
    val storeRDD = (rdd: RDD[String], time: Time) => {
      val hadoopConf = new Configuration()
      hadoopConf.set("mapreduce.output.basename", time.toString())
      rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$path", classOf[NullWritable], classOf[String],
        classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
    }
    stream.foreachRDD(storeRDD)
  }

  /**
    * store captured running time into the given path
    *
    * @param duration   duration of the training
    * @param resultPath result path
    */
  def storeTrainingTimes(duration: Long, resultPath: String) = {
    val file = new File(s"$resultPath/training-times.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    fw.write(s"$duration\n")
    fw.close()
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
    val model = trainModel(ssc.sparkContext, initialDataDirectories)
    new OnlineSVM().setInitialModel(model).setNumIterations(1).setStepSize(onlineStepSize)
  }

  /**
    * Train a SVM Model from the data in the specified directories separated by comma
    *
    * @param sc           SparkContext object
    * @param trainingPath list of directories separated by comma
    * @return SVMModel
    */
  def trainModel(sc: SparkContext, trainingPath: String): SVMModel = {
    trainModel(sc.textFile(trainingPath).map(dataParser.parsePoint))
  }

  /**
    * Train a SVM Model from the RDD
    *
    * @param data rdd
    * @return SVMModel
    */
  def trainModel(data: RDD[LabeledPoint]): SVMModel = {
    val cachedData = data.cache()
    cachedData.count()
    val model = SVMWithSGD.train(cachedData, numIterations, offlineStepSize, 0.01)
    cachedData.unpersist(false)
    model
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
    val rdd = ssc.sparkContext.textFile(path).map(dataParser.parsePoint)
    new ConstantInputDStream[LabeledPoint](ssc, rdd)
  }


  protected def createTempFolders(path: String): Unit = {
    val fs = FileSystem.get(new Configuration())
    fs.mkdirs(new Path(path))
  }


  def getApplicationName: String

  def getExperimentName: String

  def defaultBatchDuration: Long

  def defaultTrainingSlack: Long

  def run(args: Array[String])

}
