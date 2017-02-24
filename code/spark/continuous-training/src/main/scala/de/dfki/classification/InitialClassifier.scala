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
    val (batchDuration, resultRoot, initialDataPath,
    streamingDataPath, testDataPath, errorType) = parseArgs(args)
    val ssc = initializeSpark(Seconds(batchDuration))
    var testType = ""
    if (testDataPath == "prequential"){
      testType = s"prequential-$fadingFactor"
    } else {
      testType = "dataset"
    }
    val parent = s"$getExperimentName/batch-$batchDuration-slack-none-incremental-false-error-$errorType-$testType"
    val resultPath = experimentResultPath(resultRoot, parent)
    // train initial model
    streamingModel = createInitialStreamingModel(ssc, initialDataPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    // evaluate the stream
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(parsePoint), resultPath, errorType)
    } else {
      evaluateStream(testData, resultPath, errorType)
    }

    ssc.start()
    ssc.awaitTermination()

  }


  override def getApplicationName = "Baseline SVM Model"

  override def getExperimentName = "baseline"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 0L
}
