package de.dfki.deployment

import de.dfki.core.scheduling.FixedIntervalScheduler
import de.dfki.utils.CommandLineParser

/**
  * Novel training and testing model
  * Online training is supplemented with occasional one iteration of SGD on the historical data
  *
  *
  * Algorithm:
  * 1. Train initial model
  * 2. Write the incoming data into persistent storage
  * 3. Evaluate on the incoming data
  * 4. Train Incrementally on the incoming data
  * 5. periodically perform 1 iteration of SGD on full (or a sample of)historical data
  *
  * @author Behrouz Derakhshan
  */
object ContinuousClassifier extends Classifier {
  var slack: Long = _
  var incremental: Boolean = _
  var continuousStepSize: Double = _
  var samplingRate: Double = _

  val DEFAULT_SAMPLING_RATE = 0.1

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
    // optional parameter for step of size of sgd iterations in continuous deployment method
    continuousStepSize = parser.getDouble("continuous-step-size", onlineStepSize)
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
      s"slack-$slack/offline-step-$stepSize/online-step-$onlineStepSize/continuous-step-$continuousStepSize"

    val resultPath = experimentResultPath(resultRoot, child)
    if (modelPath == DEFAULT_MODEL_PATH) {
      modelPath = s"$resultRoot/$child/model"
    }

    val ssc = initializeSpark()
    //ssc.sparkContext.setLogLevel("INFO")

    // train initial model
    val startTime = System.currentTimeMillis()

    streamingModel = createInitialStreamingModel(ssc, initialDataPath, modelType)
    val endTime = System.currentTimeMillis()
    storeTrainingTimes(endTime - startTime, resultPath)

    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, evaluationDataPath)


    // evaluate the stream and incrementally update the model
    if (evaluationDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData.map(dataParser.parsePoint), resultPath)
    }

    if (incremental) {
      trainOnStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint))
    }

    // periodically schedule one iteration of the SGD
    val task = new Runnable {
      def run() {
        streamingSource.pause()
        val startTime = System.currentTimeMillis()
        val historicalDataRDD = ssc.sparkContext.textFile(initialDataPath + "," + streamingSource.getProcessedFiles.mkString(","))
          .map(dataParser.parsePoint)
          .sample(withReplacement = false, samplingRate)
        streamingModel.setStepSize(continuousStepSize).trainOn(historicalDataRDD)
        streamingModel.setStepSize(onlineStepSize)
        val endTime = System.currentTimeMillis()
        storeTrainingTimes(endTime - startTime, resultPath)
        streamingSource.resume()
      }
    }

    val scheduler = new FixedIntervalScheduler(streamingSource, ssc, task, slack)

    ssc.start()
    scheduler.init()
    scheduler.schedule()
  }

  override def getApplicationName = "Continuous Classifier"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 5L

  override def defaultModelType = "lr"
}
