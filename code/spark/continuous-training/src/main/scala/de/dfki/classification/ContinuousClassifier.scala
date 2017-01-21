package de.dfki.classification

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.utils.MLUtils.parsePoint
import org.apache.log4j.Logger

/**
  * Novel training and testing model
  * Online training is supplemented with occasional one iteration of SGD on the historical data
  *
  * @author Behrouz Derakhshan
  */
object ContinuousClassifier extends SVMClassifier {
  val slack = 5l
  var now = 0l
  @transient val logger = Logger.getLogger(getClass.getName)


  def main(args: Array[String]) {
    run(args)
  }

  override def getApplicationName(): String = "Continuous SVM Model"

  override def run(args: Array[String]): Unit = {
    createTempFolders(historicalData)
    val (initialDataPath, streamingDataPath, testDataPath) = parseArgs(args, BASE_DATA_DIRECTORY)

    val ssc = initializeSpark()

    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + historicalData)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    prequentialStreamEvaluation(streamingSource.map(_._2.toString).map(parsePoint), "results/continuous")

    val task = new Runnable {
      def run() = {
        if (ssc.sparkContext.isStopped) {
          future.cancel(true)
        }
        logger.info("schedule an iteration of SGD")
        streamingSource.pause()
        val historicalDataRDD = ssc.sparkContext.textFile(initialDataPath + "," + historicalData).map(parsePoint).cache()
        streamingModel.trainOn(historicalDataRDD)
        logger.info("model was updated")
        if (streamingSource.isCompleted()) {
          logger.warn("stopping the program")
          ssc.stop(true, true)
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
}
