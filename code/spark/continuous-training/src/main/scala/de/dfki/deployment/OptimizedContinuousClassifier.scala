package de.dfki.deployment

import de.dfki.utils.CommandLineParser
import org.apache.spark.streaming.Seconds

/**
  * @author behrouz
  */
object OptimizedContinuousClassifier extends Classifier {
  var slack: Long = _
  var incremental: Boolean = _
  var continuousStepSize: Double = _
  var samplingRate: Double = _

  val DEFAULT_SAMPLING_RATE = 0.2

  /**
    * @param args arguments to the main class should be a set of key, value pairs in the format of key=value
    *             Continuous Classifier:
    *             slack: delay between in periodic sgd iteration
    *             refer to [[Classifier]] to view the rest of the arguments
    *
    */
  def main(args: Array[String]) {
    run(args)
  }

  override def parseArgs(args: Array[String]) {
    // must be called to initialize the common parameters
    super.parseArgs(args)
    val parser = new CommandLineParser(args).parse()
    slack = parser.getLong("slack", defaultTrainingSlack)
    incremental = parser.getBoolean("incremental", default = true)
    samplingRate = parser.getDouble("sampling-rate", DEFAULT_SAMPLING_RATE)
  }

  override def run(args: Array[String]): Unit = {
    parseArgs(args)
    var testType = ""
    if (evaluationDataPath == "prequential") {
      testType = s"prequential"
    } else {
      testType = "dataset"
    }
    val child = s"$getExperimentName/model-type-$modelType/num-iterations-$numIterations/" +
      s"slack-$slack/updater-adam/step-size-$stepSize/"

    val resultPath = experimentResultPath(resultRoot, child)

    val ssc = initializeSpark()

    // train initial model
    val startTime = System.currentTimeMillis()

    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType)
    val endTime = System.currentTimeMillis()
    storeTrainingTimes(endTime - startTime, resultPath)

    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, evaluationDataPath)

    def historicalDataRDD = ssc.sparkContext.textFile(initialDataPath + "," + streamingSource.getProcessedFiles.mkString(","))
      .map(dataParser.parsePoint)
      .sample(withReplacement = false, samplingRate)


    streamingSource
      .map(_._2.toString)
      // parse input
      .map(dataParser.parsePoint)
      // online training and updating the statistics
      .transform(rdd => streamingModel.trainOn(rdd))
      // create a window
      .window(Seconds(slack), Seconds(slack))
      // hybrid proactive training
      .transform(rdd => streamingModel.trainOnHybrid(rdd, historicalDataRDD))
      // dummy action
      .foreachRDD(_ => dummyAction())
    // do nothing


    // evaluate the stream and incrementally update the model
    if (evaluationDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData.map(dataParser.parsePoint), resultPath)
    }


    ssc.start()
    ssc.awaitTermination()

  }

  override def getApplicationName = "Optimized Continuous Classifier"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 5L

  override def defaultModelType = "lr"
}