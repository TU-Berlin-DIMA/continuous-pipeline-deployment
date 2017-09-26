package de.dfki.deployment

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.evaluation.{ConfusionMatrix, LogisticLoss}
import de.dfki.ml.optimization.AdvancedUpdaters
import de.dfki.ml.streaming.models.{HybridLR, HybridModel, HybridSVM}
import de.dfki.preprocessing.parsers.{CSVParser, CustomVectorParser, DataParser, SVMParser}
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{ConstantInputDStream, DStream}


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
  * updater: [[de.dfki.ml.optimization.AdvancedUpdaters]] used for learning rate tuning
  *
  * [[ContinuousClassifier]] and [[VeloxClassifier]] require extra arguments
  *
  * @author Behrouz Derakhshan
  */
abstract class Classifier extends Serializable {

  @transient var future: ScheduledFuture[_] = _
  @transient var execService: ScheduledExecutorService = _
  @transient val logger = Logger.getLogger(getClass.getName)

  // time captured at the beginning of the experiments. Used for generating unique ids
  private val experimentTime = Calendar.getInstance().getTime

  // constants for the directory structures
  val DATA_DIRECTORY = "data"
  val DATA_SET = "criteo-full"
  val BASE_DATA_DIRECTORY: String = s"$DATA_DIRECTORY/$DATA_SET"
  val INITIAL_TRAINING = "initial-training/0"
  val STREAM_TRAINING = "processed/*"
  val TEST_DATA = "test"
  val DEFAULT_NUMBER_OF_ITERATIONS = 500
  val STEP_SIZE = 0.001
  val DEFAULT_MODEL_PATH = "generated"
  val DEFAULT_UPDATER = "adam"

  var streamingModel: HybridModel[_, _] = _
  var dataParser: DataParser = _

  def experimentResultPath(root: String, child: String): String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val experimentId = dateFormat.format(experimentTime)
    s"$root/$child/$experimentId"
  }

  var numIterations: Int = _
  var evaluationMetric: String = _
  var batchDuration: Long = _
  var stepSize: Double = _
  var defaultParallelism: Int = _
  var modelPath: String = _
  var resultRoot: String = _
  var initialDataPath: String = _
  var streamingDataPath: String = _
  var evaluationDataPath: String = _
  var modelType: String = _
  var updater: String = _


  def parseArgs(args: Array[String]) = {
    val parser = new CommandLineParser(args).parse()
    // spark streaming batch duration
    batchDuration = parser.getLong("batch-duration", defaultBatchDuration)
    // path for storing experiments results
    resultRoot = parser.get("result-path", s"../../../experiment-results/$DATA_SET")
    // folder path for initial training data
    initialDataPath = parser.get("initial-training-path", s"$BASE_DATA_DIRECTORY/$INITIAL_TRAINING")
    // folder path for data to be streamed
    streamingDataPath = parser.get("streaming-path", s"$BASE_DATA_DIRECTORY/$STREAM_TRAINING")
    // folder (file) for test data
    evaluationDataPath = parser.get("test-path", "prequential")
    // model type
    modelType = parser.get("model-type", defaultModelType)
    // cumulative test error
    evaluationMetric = parser.get("evaluation-metric", "logloss")
    // number of iterations
    numIterations = parser.getInteger("num-iterations", DEFAULT_NUMBER_OF_ITERATIONS)
    // offline learner step size
    stepSize = parser.getDouble("step-size", STEP_SIZE)
    // optional model path parameter, if not provided the model is searched in the experiment
    modelPath = parser.get("model-path", DEFAULT_MODEL_PATH)
    // updater type
    updater = parser.get("updater", DEFAULT_UPDATER)


    val inputFormat = parser.get("input-format", "vector")
    if (inputFormat == "text") {
      dataParser = new CSVParser()
    } else if (inputFormat == "svm") {
      dataParser = new SVMParser(parser.getInteger("feature-size"))
    } else {
      dataParser = new CustomVectorParser()
    }
  }

  /**
    * Initialization of spark streaming context
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
    //ssc.checkpoint("checkpoints/")
    ssc
  }

  def evaluateStream(stream: RDD[LabeledPoint],
                     evaluationData: RDD[LabeledPoint],
                     resultPath: String) = {
    val testData = evaluationDataPath match {
      case "prequential" => stream
      case _ => evaluationData
    }
    evaluationMetric match {
      case "logloss" =>
        val totalLogLoss = streamingModel
          .predictOnValues(testData.map(lp => (lp.label, lp.features)))
          .map(pre => (LogisticLoss.logisticLoss(pre._1, pre._2), 1))
          // sum over logistic loss
          .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
        // store the average logistic loss into file
        storeLogisticLoss(totalLogLoss._1 / totalLogLoss._2, resultPath)
      case "confusion-matrix" =>
        val cm = streamingModel
          .predictOnValues(testData.map(lp => (lp.label, lp.features)))
          .map {
            v =>
              var tp, fp, tn, fn = 0
              if (v._1 == v._2 & v._1 == 1.0) tp = 1
              else if (v._1 == v._2 & v._1 == 0.0) tn = 1
              else if (v._1 != v._2 & v._1 == 1.0) fp = 1
              else fn = 1
              new ConfusionMatrix(tp, fp, tn, fn)
          }
          .reduce((c1, c2) => ConfusionMatrix.merge(c1, c2))
        storeConfusionMatrix(cm, resultPath)
    }
    stream
  }

  val storeLogisticLoss = (logLoss: Double, resultPath: String) => {
    val file = new File(s"$resultPath/loss.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$logLoss\n")
    }
    finally fw.close()
  }

  private val storeConfusionMatrix = (confusionMatrix: ConfusionMatrix, resultPath: String) => {
    val file = new File(s"$resultPath/confusion-matrix.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"${confusionMatrix.resultAsCSV}\n")
    }
    finally fw.close()
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
  def createInitialStreamingModel(ssc: StreamingContext, initialDataDirectories: String, modelType: String): HybridModel[_, _] = {
    if (Files.exists(Paths.get(modelPath))) {
      logger.info("Model exists, loading the model from disk ...")
      val model = HybridModel.loadFromDisk(modelPath)
      model
        // sampling is done manually
        .setMiniBatchFraction(1.0)
        .setRegParam(0.001)
        .setConvergenceTol(0.0)
        .setNumIterations(5)
    } else {
      val hybridModel = if (modelType.equals("svm")) {
        logger.info("Instantiating a SVM Model")
        new HybridSVM(stepSize, numIterations, 0.0, 0.1, AdvancedUpdaters.getUpdater(updater))
          .setConvergenceTol(0.0)
      } else {
        logger.info("Instantiating a Linear Regression Model")
        // regularization parameter is chosen from GridSearch
        new HybridLR(stepSize, numIterations, 0.0, 0.1, AdvancedUpdaters.getUpdater(updater))
          .setConvergenceTol(0.0)
      }
      val data = ssc.sparkContext.textFile(initialDataDirectories)
        .map(dataParser.parsePoint)
        .repartition(ssc.sparkContext.defaultParallelism)
        .cache()
      hybridModel.trainInitialModel(data)
      data.unpersist()

      // save the model to disk, consecutive runs will check this directory first
      HybridModel.saveToDisk(modelPath, hybridModel)
      hybridModel
        .setMiniBatchFraction(1.0)
        .setConvergenceTol(0.0)
        .setNumIterations(5)
    }
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
  def constantInputDStreaming(ssc: StreamingContext, path: String): DStream[String] = {
    val rdd = ssc.sparkContext.textFile(path)
    new ConstantInputDStream[String](ssc, rdd)
  }


  def dummyAction() = {
  }


  def getApplicationName: String

  def getExperimentName: String

  def defaultBatchDuration: Long

  def defaultTrainingSlack: Long

  def defaultModelType: String

  def run(args: Array[String])

}
