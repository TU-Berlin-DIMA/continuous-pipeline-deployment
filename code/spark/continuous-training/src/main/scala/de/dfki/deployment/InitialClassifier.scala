package de.dfki.deployment

/**
  * Baseline classifier
  * Train an initial model and deploy it without further incremental learning
  *
  * @author Behrouz Derakhshan
  */
object InitialClassifier extends Classifier {

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    val (resultRoot, initialDataPath, streamingDataPath, testDataPath, modelType) = parseArgs(args)
    val ssc = initializeSpark()
    var testType = ""
    if (testDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val child = s"$getExperimentName/model-type-$modelType/num-iterations-$numIterations/" +
      s"slack-none/offline-step-$offlineStepSize/online-step-$onlineStepSize"

    val resultPath = experimentResultPath(resultRoot, child)
    val modelPath = s"$resultRoot/$child/model"
    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType, modelPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // evaluate the stream
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData, resultPath)
    }

    ssc.start()
    ssc.awaitTermination()

  }


  override def getApplicationName = "Baseline Classifier"

  override def getExperimentName = "baseline"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L

  override def defaultModelType = "svm"
}
