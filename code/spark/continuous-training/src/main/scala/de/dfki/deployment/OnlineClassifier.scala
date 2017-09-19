package de.dfki.deployment

/**
  * Baseline+ classifier
  * Train an initial model and apply incremental learning after deployment
  *
  * @author Behrouz Derakhshan
  */
object OnlineClassifier extends Classifier {

  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    parseArgs(args)
    val ssc = initializeSpark()
    var testType = ""
    if (evaluationDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val child = s"$getExperimentName/model-type-$modelType/num-iterations-$numIterations/" +
      s"slack-none/offline-step-$offlineStepSize/online-step-$onlineStepSize"

    val resultPath = experimentResultPath(resultRoot, child)
    if (modelPath == DEFAULT_MODEL_PATH) {
      modelPath = s"$resultRoot/$child/model"
    }
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, evaluationDataPath)

    // evaluate the stream and incrementally update the model
    if (evaluationDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData.map(dataParser.parsePoint), resultPath)
    }

    trainOnStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint))


    ssc.start()
    ssc.awaitTermination()
  }

  override def parseArgs(args: Array[String]) = super.parseArgs(args)

  override def getApplicationName: String = "Baseline+ Classifier"

  override def getExperimentName = "baseline-plus"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L

  override def defaultModelType = "lr"
}
