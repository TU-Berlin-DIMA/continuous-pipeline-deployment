package de.dfki.classification

import java.io.{File, FileWriter}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}

import de.dfki.streaming.models.OnlineSVM
import de.dfki.utils.MLUtils.parsePoint
import de.dfki.utils.{BatchFileInputDStream, CommandLineParser}
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

  def experimentResultPath(root: String): String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val experimentId = dateFormat.format(experimentTime)
    s"$root/$experimentId"
  }


  /**
    * Initialization of spark streaming context and checkpointing of stateful operators
    *
    * @return Spark Streaming Context object
    */
  def initializeSpark(batchDuration: Duration = Seconds(1)): StreamingContext = {
    val conf = new SparkConf().setAppName(getApplicationName)
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val ssc = new StreamingContext(conf, batchDuration)
    ssc.checkpoint("checkpoints/")
    ssc
  }

  /**
    * Store the cumulative error rate of the incoming stream in the given result directory
    *
    * @param testData   test Data DStream
    * @param resultPath directory for writing the error rate results
    */
  def evaluateStream(testData: DStream[LabeledPoint], resultPath: String) {
    val storeErrorRate = (rdd: RDD[Double]) => {
      val file = new File(s"${experimentResultPath(resultPath)}/error-rates.txt")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        // TODO: it should not write to file when the rdd is empty
        val content = rdd.collect().toList.mkString("\n")
        if (content == "") {}
        else {
          fw.write(s"$content\n")
        }
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
      .map(item => item._1 / item._2)
      .foreachRDD(storeErrorRate)

    streamingModel.writeToDisk(testData, experimentResultPath(resultPath))
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
    val file = new File(s"${experimentResultPath(resultPath)}/training-times.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    fw.write(s"$duration\n")
    fw.close()
  }

  //  /**
  //    * Prequential Evaluation of the stream. First use the data to predict and train the model
  //    *
  //    * @param observations training data
  //    */
  //  def prequentialStreamEvaluation(observations: DStream[LabeledPoint], resultPath: String): Unit = {
  //    evaluateStream(observations, resultPath)
  //  }

  def parseArgs(args: Array[String]): (Long, String, String, String, String) = {
    val parser = new CommandLineParser(args).parse()
    // spark streaming batch duration
    val batchDuration = parser.getLong("batch-duration", defaultBatchDuration)
    // path for storing experiments results
    val resultPath = parser.get("result-path", s"results/$DATA_SET/$getExperimentName")
    // folder path for initial training data
    val initialDataPath = parser.get("initial-training-path", s"$BASE_DATA_DIRECTORY/$INITIAL_TRAINING")
    // folder path for data to be streamed
    val streamingDataPath = parser.get("streaming-path", s"$BASE_DATA_DIRECTORY/$STREAM_TRAINING")
    // folder (file) for test data
    val testDataPath = parser.get("test-path", "prequential")

    (batchDuration, resultPath, initialDataPath, streamingDataPath, testDataPath)

  }

  /**
    * Initialize an Online SVM model by first using the data in the given directory to train a static model
    * and then load the model into the Online SVM model
    *
    * @param ssc                    Spark Streaming Context
    * @param initialDataDirectories directory of initial data
    * @return Online SVM Model
    */
  def createInitialStreamingModel(ssc: StreamingContext, initialDataDirectories: String, numIterations: Int = 100): OnlineSVM = {
    val model = trainModel(ssc.sparkContext, initialDataDirectories, numIterations)
    new OnlineSVM().setInitialModel(model).setNumIterations(10).setStepSize(0.001)
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

  def getApplicationName: String

  def getExperimentName: String

  def defaultBatchDuration: Long

  def defaultTrainingSlack: Long

  def run(args: Array[String])

}
