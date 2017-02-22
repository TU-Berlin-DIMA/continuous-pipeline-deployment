package de.dfki.classification

import de.dfki.utils.MLUtils.parsePoint
import org.apache.log4j.Logger
import org.apache.spark.streaming.Seconds

/**
  * Baseline+ classifier
  * Train an initial model and apply incremental learning after deployment
  *
  * @author Behrouz Derakhshan
  */
object OnlineClassifier extends SVMClassifier {
  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    val (batchDuration, resultPath, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)

    val ssc = initializeSpark(Seconds(batchDuration))

    streamingModel = createInitialStreamingModel(ssc, initialDataPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // evaluate the stream and incrementally update the model
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(parsePoint), resultPath)
      trainOnStream(streamingSource.map(_._2.toString).map(parsePoint))
    } else {
      evaluateStream(testData, resultPath)
      trainOnStream(streamingSource.map(_._2.toString).map(parsePoint))
    }

    ssc.start()
    ssc.awaitTermination()
  }

  override def getApplicationName: String = "Online SVM Model"

  override def getExperimentName = "online"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L
}
