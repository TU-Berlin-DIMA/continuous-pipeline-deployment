package de.dfki.classification

import de.dfki.utils.MLUtils.parsePoint
import org.apache.spark.streaming.Seconds

/**
  * Baseline classifier
  * Train an initial model and deploy it without further incremental learning
  *
  * @author Behrouz Derakhshan
  */
object InitialClassifier extends SVMClassifier {

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    val (batchDuration, resultPath, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args)
    val ssc = initializeSpark(Seconds(batchDuration))

    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, 100)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // evaluate the stream
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(parsePoint), resultPath)
    } else {
      evaluateStream(testData, resultPath)
    }

    ssc.start()
    ssc.awaitTermination()

  }


  override def getApplicationName = "Initial SVM Model"

  override def getExperimentName = "initial-only"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L
}
