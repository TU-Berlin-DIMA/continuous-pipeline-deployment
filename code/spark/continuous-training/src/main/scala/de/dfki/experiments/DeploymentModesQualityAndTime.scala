package de.dfki.experiments

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.baseline.BaselineDeployment
import de.dfki.deployment.continuous.ContinuousDeploymentWithOptimizations
import de.dfki.deployment.online.OnlineDeployment
import de.dfki.deployment.periodical.MultiOnlineWithWarmStartingDeployment
import de.dfki.experiments.profiles.URLProfile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object DeploymentModesQualityAndTime extends Experiment {

  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/deployment-modes"
  }

  def main(args: Array[String]): Unit = {

    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Quality and Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    var ssc = new StreamingContext(conf, Seconds(1))

    // online only training
    val onlinePipeline = getPipeline(ssc.sparkContext, params)
    new OnlineDeployment(
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      otherParams = params).deploy(ssc, onlinePipeline)
    ssc.stop(stopSparkContext = true, stopGracefully = true)

    // Continuously train with full optimization set
    ssc = new StreamingContext(conf, Seconds(1))
    val continuousPipelineWithOptimization = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentWithOptimizations(history = params.inputPath,
      streamBase = params.streamPath,
      materializeBase = params.materializedPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.sampleSize),
      otherParams = params).deploy(ssc, continuousPipelineWithOptimization)
    ssc.stop(stopSparkContext = true, stopGracefully = true)

    // Baseline Deployment
    ssc = new StreamingContext(conf, Seconds(1))
    val baselinePipeline = getPipeline(ssc.sparkContext, params)
    new BaselineDeployment(streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days
    ).deploy(ssc, baselinePipeline)
    ssc.stop(stopSparkContext = true, stopGracefully = true)

    // Periodical Deployment
    ssc = new StreamingContext(conf, Seconds(1))
    val periodicalPipelineWarm = getPipeline(ssc.sparkContext, params)
    new MultiOnlineWithWarmStartingDeployment(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = params.evaluationPath,
      resultPath = params.resultPath,
      daysToProcess = params.days,
      frequency = params.trainingFrequency,
      numPartitions = params.numPartitions,
      otherParams = params,
      sparkConf = conf
    ).deploy(ssc, periodicalPipelineWarm)
  }
}
