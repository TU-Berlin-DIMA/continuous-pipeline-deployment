package de.dfki.experiments

import de.dfki.core.sampling.WindowBasedSampler
import de.dfki.deployment.continuous.ContinuousDeploymentWithOptimizations
import de.dfki.deployment.rolling.RollingRetraining
import de.dfki.experiments.profiles.URLProfile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object RollingQualityAndTime extends Experiment {
  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/deployment-modes"
    override val TRAINING_FREQUENCY = 100
    override val ROLLING_WINDOW = 700
  }

  def main(args: Array[String]): Unit = {

    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Quality and Time Experiment Rolling ")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    var ssc = new StreamingContext(conf, Seconds(1))
    val continuousPipelineWithOptimization = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentWithOptimizations(history = params.inputPath,
      streamBase = params.streamPath,
      materializeBase = params.materializedPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      //sampler = new RollingWindowProvider(size = params.rollingWindow),
      sampler = new WindowBasedSampler(size = params.sampleSize, window = params.rollingWindow),
      otherParams = params,
      online = params.online).deploy(ssc, continuousPipelineWithOptimization)
    ssc.stop(stopSparkContext = true, stopGracefully = true)

    // Periodical Deployment
    ssc = new StreamingContext(conf, Seconds(1))
    val periodicalPipelineWarm = getPipeline(ssc.sparkContext, params)
    new RollingRetraining(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = params.evaluationPath,
      resultPath = params.resultPath,
      daysToProcess = params.days,
      frequency = params.trainingFrequency,
      rollingWindowSize = params.rollingWindow,
      numPartitions = params.numPartitions,
      otherParams = params,
      sparkConf = conf,
      online = params.online
    ).deploy(ssc, periodicalPipelineWarm)
  }
}
