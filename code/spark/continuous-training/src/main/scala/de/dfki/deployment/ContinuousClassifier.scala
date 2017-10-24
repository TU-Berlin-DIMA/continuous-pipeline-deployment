package de.dfki.deployment

import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Seconds

/**
  * @author behrouz
  */
object ContinuousClassifier extends Classifier {
  var slack: Long = _
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
    val child = s"$getExperimentName/num-iterations-$numIterations/slack-$slack/updater-$updater/step-size-$stepSize/"

    val resultPath = experimentResultPath(resultRoot, child)

    val ssc = initializeSpark()

    // train initial model
    val startTime = System.currentTimeMillis()

    val data = ssc.sparkContext
      .textFile(initialDataPath)
      .repartition(ssc.sparkContext.defaultParallelism)

    val pipeline = trainInitialPipeline(ssc, data)
    val endTime = System.currentTimeMillis()
    storeTrainingTimes(endTime - startTime, resultPath)

    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = ssc.sparkContext.textFile(evaluationDataPath)

    def historicalDataRDD(recentItems: RDD[String]) = {
      logger.info(s"scheduling a batch iteration on ${streamingSource.getProcessedFiles.length} files ")
      ssc.sparkContext.textFile(streamingSource.getProcessedFiles.mkString(","))
        .union(data)
        .union(recentItems)
        .sample(withReplacement = false, samplingRate)
        .repartition(ssc.sparkContext.defaultParallelism)
        .cache()
    }

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)

    evaluateStream(pipeline, testData, testData, resultPath)
    streamingSource
      .map(_._2.toString)
      // updating the statistics
      .transform(rdd => {
      pipeline.update(rdd)
      rdd
    })
      // create a window
      .window(Seconds(slack), Seconds(slack))
      // hybrid proactive training
      .transform(rdd => {
      pipeline.train(historicalDataRDD(rdd))
      rdd
    })
      // evaluate the model
      .transform(_ => evaluateStream(pipeline, testData, testData, resultPath))
      // dummy action
      .foreachRDD(_ => dummyAction())


    ssc.start()
    ssc.awaitTermination()

  }

  override def getApplicationName = "Optimized Continuous Classifier"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 5L

  override def defaultModelType = "lr"
}