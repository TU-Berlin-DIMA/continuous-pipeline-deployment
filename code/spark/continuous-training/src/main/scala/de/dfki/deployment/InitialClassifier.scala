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
    parseArgs(args)
    val ssc = initializeSpark()
    var testType = ""
    if (evaluationDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val child = s"$getExperimentName/model-type-$modelType/num-iterations-$numIterations/" +
      s"slack-none/offline-step-$offlineStepSize"

    val resultPath = experimentResultPath(resultRoot, child)
    if (modelPath == DEFAULT_MODEL_PATH) {
      modelPath = s"$resultRoot/$child/model"
    }
    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, evaluationDataPath)

    // evaluate the stream
    if (evaluationDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData.map(dataParser.parsePoint), resultPath)
    }

    ssc.start()
    ssc.awaitTermination()

  }


  override def parseArgs(args: Array[String]) = super.parseArgs(args)

  override def getApplicationName = "Baseline Classifier"

  override def getExperimentName = "baseline"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L

  override def defaultModelType = "lr"
}
