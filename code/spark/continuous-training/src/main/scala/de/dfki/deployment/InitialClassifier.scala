package de.dfki.deployment

import de.dfki.deployment.ContinuousClassifier._

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
      s"slack-0/updater-$updater/step-size-$stepSize/"

    val resultPath = experimentResultPath(resultRoot, child)
    if (modelPath == DEFAULT_MODEL_PATH) {
      modelPath = s"$resultRoot/$child/model"
    }
    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = ssc.sparkContext.textFile(evaluationDataPath).map(dataParser.parsePoint)

    streamingSource
      .map(_._2.toString)
      // parse input
      .map(dataParser.parsePoint)
      // evaluate the model
      .transform(rdd => evaluateStream(rdd, testData, resultPath))
      // dummy action
      .foreachRDD(_ => dummyAction())

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
