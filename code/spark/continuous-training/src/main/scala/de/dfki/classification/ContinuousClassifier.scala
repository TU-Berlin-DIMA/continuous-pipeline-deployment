package de.dfki.classification

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.utils.CommandLineParser
import de.dfki.utils.MLUtils.parsePoint
import org.apache.log4j.Logger
import org.apache.spark.streaming.Seconds

/**
  * Novel training and testing model
  * Online training is supplemented with occasional one iteration of SGD on the historical data
  *
  * @author Behrouz Derakhshan
  */
object ContinuousClassifier extends SVMClassifier {
  @transient val logger = Logger.getLogger(getClass.getName)

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

  def parseContinuousArgs(args: Array[String]): (Long, Long, String, String, String, String, String) = {
    val parser = new CommandLineParser(args).parse()
    val (batchDuration, resultPath, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)
    val slack = parser.getLong("slack", defaultTrainingSlack)
    val tempDirectory = parser.get("temp-path", s"$BASE_DATA_DIRECTORY/temp-data")
    (batchDuration, slack, resultPath, initialDataPath, streamingDataPath, testDataPath, experimentResultPath(tempDirectory))
  }

  override def run(args: Array[String]): Unit = {
    val (batchDuration, slack, resultPath, initialDataPath, streamingDataPath, testDataPath, tempDirectory) = parseContinuousArgs(args)
    createTempFolders(tempDirectory)
    val ssc = initializeSpark(Seconds(batchDuration))

    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + tempDirectory)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    writeStreamToDisk(streamingSource.map(_._2.toString), tempDirectory)
    if (testDataPath == "prequential") {
      prequentialStreamEvaluation(streamingSource.map(_._2.toString).map(parsePoint), resultPath)
    } else {
      streamProcessing(testData, streamingSource.map(_._2.toString).map(parsePoint), resultPath)
    }

    val task = new Runnable {
      def run() {
        if (ssc.sparkContext.isStopped) {
          future.cancel(true)
        }
        logger.info("schedule an iteration of SGD")
        streamingSource.pause()
        val startTime = System.currentTimeMillis()
        val historicalDataRDD = ssc.sparkContext.textFile(initialDataPath + "," + tempDirectory).map(parsePoint).cache()
        streamingModel.trainOn(historicalDataRDD)
        val endTime = System.currentTimeMillis()
        storeTrainingTimes(endTime - startTime, resultPath)
        logger.info("model was updated")
        if (streamingSource.isCompleted()) {
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
    future = execService.scheduleAtFixedRate(task, slack, slack, TimeUnit.SECONDS)
    future.get()


  }

  override def getApplicationName = "Continuous SVM Model"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 20L
}
