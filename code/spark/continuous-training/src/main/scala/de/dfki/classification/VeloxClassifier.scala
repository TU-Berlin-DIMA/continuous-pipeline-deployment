package de.dfki.classification

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.streaming.Seconds

/**
  * Training and Serving of SVM classifier using the architecture described in Velox: https://arxiv.org/abs/1409.3809
  * - Train a model on an initial dataset
  * - 'Deploy' the model and start relieving prediction queries and observations
  * - On prediction queries return the predicted label
  * - On training observations perform incremental update (to maximize throughput, data is arriving in mini batches)
  * - Retrain a model after a while, discard the current model and 'deploy' the new model
  *
  * TODO: This is a hackish solution ... Full implementation should trigger a complete restart of the spark context ..
  * Perform a batch retraining ... store the model and read the model in the streaming as the initial model
  *
  * @author Behrouz Derakhshan
  */
object VeloxClassifier extends SVMClassifier {


  /**
    * @param args arguments to the main class should be a set of key, value pairs in the format of key=value
    *             Velox Classifier:
    *             slack: delay between in periodic sgd iteration
    *             temp-path: path to write the observed data for retraining purposed
    *             refer to [[SVMClassifier]] to view the rest of the arguments
    *
    */
  def main(args: Array[String]): Unit = {
    run(args)
  }

  def parseVeloxArgs(args: Array[String]): (Long, String, String, String, String, String, Boolean) = {
    val parser = new CommandLineParser(args).parse()
    val (resultPath, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)
    val slack = parser.getLong("slack", defaultTrainingSlack)
    val tempDirectory = parser.get("temp-path", s"$BASE_DATA_DIRECTORY/temp-data")
    val incremental = parser.getBoolean("incremental", default = true)
    (slack, resultPath, initialDataPath, streamingDataPath,
      testDataPath, tempDirectory, incremental)
  }

  override def run(args: Array[String]): Unit = {
    val (slack, resultRoot, initialDataPath, streamingDataPath, testDataPath, tempRoot, incremental) = parseVeloxArgs(args)
    var testType = ""
    if (testDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val parent = s"$getExperimentName/num-iterations-$numIterations/" +
      s"slack-$slack/offline-step-$offlineStepSize/online-step-$onlineStepSize"

    val resultPath = experimentResultPath(resultRoot, parent)
    val tempDirectory = experimentResultPath(tempRoot, parent)
    createTempFolders(tempDirectory)
    val ssc = initializeSpark()

    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + tempDirectory)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    writeStreamToDisk(streamingSource.map(_._2.toString), tempDirectory)

    // evaluate the stream and incrementally update the model
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData, resultPath)
    }

    if (incremental) {
      trainOnStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint))
    }

    // periodically retrain the model from scratch using the historical data
    val task = new Runnable {
      def run() {
        if (ssc.sparkContext.isStopped) {
          future.cancel(true)
        }
        logger.info("initiating a retraining of the model ...")
        println(streamingModel.latestModel().weights.toString)
        streamingSource.pause()

        val startTime = System.currentTimeMillis()
        val before = streamingModel.latestModel().weights
        val model = trainModel(ssc.sparkContext, initialDataPath + "," + tempDirectory, numIterations)
        val after = streamingModel.latestModel().weights
        val endTime = System.currentTimeMillis()
        storeTrainingTimes(endTime - startTime, resultPath)
        streamingModel.setInitialModel(model)
        logger.info(s"Delta: ${Vectors.sqdist(before, after)}")


        if (streamingSource.isCompleted) {
          logger.warn("stopping the program")
          ssc.stop(stopSparkContext = true, stopGracefully = true)
          future.cancel(true)
          execService.shutdown()
        }

        streamingSource.unpause()
      }
    }

    ssc.start()

    execService = Executors.newSingleThreadScheduledExecutor()
    future = execService.scheduleWithFixedDelay(task, slack, slack, TimeUnit.SECONDS)
    future.get()

  }

  override def getApplicationName = "Velox SVM Model"

  override def getExperimentName = "velox"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 60L
}