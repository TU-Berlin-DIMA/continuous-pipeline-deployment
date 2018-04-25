package de.dfki.experiments

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.baseline.BaselineDeployment
import de.dfki.deployment.continuous.ContinuousDeploymentWithOptimizations
import de.dfki.deployment.online.OnlineDeployment
import de.dfki.deployment.periodical.{MultiOnlineDeployment, MultiOnlineWithWarmStartingDeployment, PeriodicalDeployment}
import de.dfki.experiments.profiles.URLProfile
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

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
    //
    var ssc = new StreamingContext(conf, Seconds(1))
    //
    //    // online only training
    //    val onlinePipeline = getPipeline(ssc.sparkContext, params)
    //    new OnlineDeployment(
    //      streamBase = params.streamPath,
    //      evaluation = s"${params.evaluationPath}",
    //      resultPath = s"${params.resultPath}",
    //      daysToProcess = params.days).deploy(ssc, onlinePipeline)
    //
    //    ssc.stop(stopSparkContext = true, stopGracefully = true)
    //    ssc = new StreamingContext(conf, Seconds(1))

    // continuously train with full optimization set
    val continuousPipelineWithOptimization = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentWithOptimizations(history = params.inputPath,
      streamBase = params.streamPath,
      materializeBase = params.materializedPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, continuousPipelineWithOptimization)


    ssc.stop(stopSparkContext = true, stopGracefully = true)
    ssc = new StreamingContext(conf, Seconds(1))

    // baseline with no online learning
    val baselinePipeline = getPipeline(ssc.sparkContext, params)
    new BaselineDeployment(streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days
    ).deploy(ssc, baselinePipeline)


    ssc.stop(stopSparkContext = true, stopGracefully = true)
    ssc = new StreamingContext(conf, Seconds(1))

    // periodical with online learning
    //    val periodicalPipeline = getPipeline(ssc.sparkContext, params)
    //    new MultiOnlineDeployment(history = params.inputPath,
    //      streamBase = params.streamPath,
    //      evaluation = s"${params.evaluationPath}",
    //      resultPath = s"${params.resultPath}",
    //      daysToProcess = params.days,
    //      frequency = params.dayDuration * 10,
    //      sparkConf = conf
    //    ).deploy(ssc, periodicalPipeline)
    //
    //    ssc.stop(stopSparkContext = true, stopGracefully = true)
    //    ssc = new StreamingContext(conf, Seconds(1))


    val periodicalPipelineWarm = getPipeline(ssc.sparkContext, params)
    new MultiOnlineWithWarmStartingDeployment(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      frequency = params.dayDuration * 10,
      sparkConf = conf
    ).deploy(ssc, periodicalPipelineWarm)
  }
}
