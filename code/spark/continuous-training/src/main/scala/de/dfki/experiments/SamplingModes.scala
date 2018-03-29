package de.dfki.experiments

import de.dfki.core.sampling._
import de.dfki.deployment.continuous.ContinuousDeploymentQualityAnalysis
import de.dfki.experiments.profiles.{Profile, URLProfile}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object SamplingModes extends Experiment {

  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "../../../experiment-results/url-reputation/sampling-modes"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/best/adam"
  }

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)
    val conf = new SparkConf().setAppName("Sampling Mode Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))

    // continuously trained with a uniform sample of the historical data
    val uniformPipeline = getPipeline(ssc.sparkContext, params)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new UniformSampler(size = params.dayDuration)).deploy(ssc, uniformPipeline)

    // continuously trained with a window based sample of the historical data
    val windowBased = getPipeline(ssc.sparkContext, params)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new WindowBasedSampler(size = params.dayDuration, window = params.dayDuration * 10)).deploy(ssc, windowBased)

    // continuously trained with a time based sample of the historical data
    val timeBasedFix = getPipeline(ssc.sparkContext, params)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.dayDuration)).deploy(ssc, timeBasedFix)
  }
}
