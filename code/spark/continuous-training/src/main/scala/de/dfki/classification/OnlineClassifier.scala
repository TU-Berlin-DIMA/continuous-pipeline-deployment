package de.dfki.classification

import de.dfki.utils.MLUtils.parsePoint
import org.apache.log4j.Logger

/**
  * @author Behrouz Derakhshan
  */
object OnlineClassifier extends SVMClassifier {
  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def getApplicationName: String = "Online SVM Model"

  override def run(args: Array[String]): Unit = {
    val (evaluationType, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args, BASE_DATA_DIRECTORY)

    val ssc = initializeSpark()

    streamingModel = createInitialStreamingModel(ssc, initialDataPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    prequentialStreamEvaluation(streamingSource.map(_._2.toString).map(parsePoint), "results/online")

    ssc.start()
    ssc.awaitTermination()
  }
}
