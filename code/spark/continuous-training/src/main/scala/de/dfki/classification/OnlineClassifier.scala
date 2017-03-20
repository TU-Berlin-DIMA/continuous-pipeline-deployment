package de.dfki.classification

import org.apache.spark.streaming.Seconds

/**
  * Baseline+ classifier
  * Train an initial model and apply incremental learning after deployment
  *
  * @author Behrouz Derakhshan
  */
object OnlineClassifier extends SVMClassifier {

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    val (batchDuration, resultRoot, initialDataPath, streamingDataPath,
    testDataPath, errorType, numIterations) = parseArgs(args)

    val ssc = initializeSpark(Seconds(batchDuration))
    var testType = ""
    if (testDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val parent = s"$getExperimentName/batch-$batchDuration/slack-none/incremental-true" +
      s"/error-$errorType-$testType/num-iterations-$numIterations"
    val resultPath = experimentResultPath(resultRoot, parent)
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, numIterations)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // evaluate the stream and incrementally update the model
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath, errorType)
    } else {
      evaluateStream(testData, resultPath, errorType)
    }

    trainOnStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint))


    ssc.start()
    ssc.awaitTermination()
  }

  override def getApplicationName: String = "Baseline+ SVM Model"

  override def getExperimentName = "baseline-plus"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L
}
