package de.dfki.classification

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.Vectors

/**
  * Novel training and testing model
  * Online training is supplemented with occasional one iteration of SGD on the historical data
  *
  *
  * Algorithm:
  * 1. Train initial model
  * 2. Write the incoming data into persistent storage
  * 3. Evaluate on the incoming data
  * 4. Train Incrementally on the incoming data
  * 5. periodically perform 1 iteration of SGD on full (or a sample of)historical data
  *
  * @author Behrouz Derakhshan
  */
object ContinuousClassifier extends SVMClassifier {

  /**
    * @param args arguments to the main class should be a set of key, value pairs in the format of key=value
    *             Continuous Classifier:
    *             slack: delay between in periodic sgd iteration
    *             temp-path: path to write the observed data for retraining purposed
    *             refer to [[SVMClassifier]] to view the rest of the arguments
    *
    */
  def main(args: Array[String]) {
    run(args)
  }

  def parseContinuousArgs(args: Array[String]): (String, Long, String, String, String, String, Boolean, Double) = {
    val parser = new CommandLineParser(args).parse()
    val (resultRoot, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)
    val slack = parser.getLong("slack", defaultTrainingSlack)
    val tempRoot = parser.get("temp-path", s"$BASE_DATA_DIRECTORY/temp-data")
    val incremental = parser.getBoolean("incremental", default = true)
    // optional parameter for step of size of sgd iterations in continuous deployment method
    val continuousStepSize = parser.getDouble("continuous-step-size", onlineStepSize)
    (resultRoot, slack, initialDataPath, streamingDataPath, testDataPath, tempRoot, incremental, continuousStepSize)
  }

  override def run(args: Array[String]): Unit = {
    val (resultRoot, slack, initialDataPath, streamingDataPath, testDataPath, tempRoot, incremental, continuousStepSize) = parseContinuousArgs(args)
    var testType = ""
    if (testDataPath == "prequential") {
      testType = s"prequential"
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
    val startTime = System.currentTimeMillis()
    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + tempDirectory)
    val endTime = System.currentTimeMillis()
    storeTrainingTimes(endTime - startTime, resultPath)

    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // store the incoming stream to disk for further re-training
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

    // periodically schedule one iteration of the SGD
    val task = new Runnable {
      def run() {
        if (ssc.sparkContext.isStopped) {
          future.cancel(true)
        }
        logger.info("schedule an iteration of SGD")
        streamingSource.pause()
        val startTime = System.currentTimeMillis()
        val historicalDataRDD = ssc.sparkContext.textFile(initialDataPath + "," + tempDirectory).map(dataParser.parsePoint).sample(withReplacement = false, fraction = 0.2).cache()
        //val before = streamingModel.latestModel().weights
        streamingModel.setStepSize(continuousStepSize)
        streamingModel.trainOn(historicalDataRDD)
        streamingModel.setStepSize(onlineStepSize)
        //val after = streamingModel.latestModel().weights
        val endTime = System.currentTimeMillis()
        storeTrainingTimes(endTime - startTime, resultPath)
        //logger.info(s"Delta: ${Vectors.sqdist(before, after)}")
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

  override def getApplicationName = "Continuous SVM Model"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 5L
}
