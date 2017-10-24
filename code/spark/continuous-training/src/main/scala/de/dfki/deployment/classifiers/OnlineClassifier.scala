package de.dfki.deployment.classifiers

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
      s"slack-0/updater-$updater/step-size-$stepSize/"

    val resultPath = experimentResultPath(resultRoot, child)
    if (pipelinePath == DEFAULT_MODEL_PATH) {
      pipelinePath = s"$resultRoot/$child/model"
    }

    val data = ssc.sparkContext
      .textFile(initialDataPath)
      .repartition(ssc.sparkContext.defaultParallelism)

    val pipeline = trainInitialPipeline(ssc, data)

    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = ssc.sparkContext.textFile(evaluationDataPath)

    streamingSource
      .map(_._2.toString)
      // evaluate the model
      .transform(rdd => evaluateStream(pipeline, rdd, testData, resultPath))
      // updating the statistics
      .transform(rdd => {
      pipeline.update(rdd)
      rdd
    })
      // train new model
      .transform(rdd => {
      pipeline.train(rdd)
      rdd
    })
      // dummy action
      .foreachRDD(_ => dummyAction())


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
