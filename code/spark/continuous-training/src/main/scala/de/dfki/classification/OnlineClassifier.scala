package de.dfki.classification

import de.dfki.utils.MLUtils.parsePoint
import org.apache.log4j.Logger
import org.apache.spark.streaming.Seconds

/**
  * @author Behrouz Derakhshan
  */
object OnlineClassifier extends SVMClassifier {
  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    val (batchDuration, _, resultPath, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)

    val ssc = initializeSpark(Seconds(batchDuration))

    streamingModel = createInitialStreamingModel(ssc, initialDataPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    prequentialStreamEvaluation(streamingSource.map(_._2.toString).map(parsePoint), resultPath)

    ssc.start()
    ssc.awaitTermination()
  }

  override def getApplicationName: String = "Online SVM Model"

  override def getExperimentName = "online"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L
}
