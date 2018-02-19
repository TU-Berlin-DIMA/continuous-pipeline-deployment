package de.dfki.deployment.classifiers

import de.dfki.core.sampling.SimpleRandomSampler
import de.dfki.deployment.ContinuousDeploymentTimeAnalysis
import de.dfki.utils.CommandLineParser

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

    val data = ssc.sparkContext
      .textFile(initialDataPath)
      .repartition(ssc.sparkContext.defaultParallelism)

    val pipeline = trainInitialPipeline(ssc, data)

    val deployment = new ContinuousDeploymentTimeAnalysis(history = initialDataPath,
      streamBase = streamingDataPath,
      evaluationPath = evaluationDataPath,
      resultPath = s"$resultPath/loss",
      slack = 1,
      sampler = new SimpleRandomSampler(0.1))

    deployment.deploy(ssc, pipeline)
  }

  override def getApplicationName = "Optimized Continuous Classifier"

  override def getExperimentName = "continuous"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 5L

  override def defaultModelType = "lr"
}