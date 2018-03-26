package de.dfki.experiments

import de.dfki.core.sampling._
import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.experiments.profiles.Profile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object SamplingModes extends Experiment {

  override val defaultProfile = new Profile {
    val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
    val STREAM_PATH = "data/url-reputation/processed/stream"
    val EVALUATION_PATH = "prequential"
    val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
    val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-3000"
    override val DELIMITER = ","
    // URL FEATURE SIZE
    // val NUM_FEATURES = 3231961
    val NUM_FEATURES = 3000
    override val NUM_ITERATIONS = 2000
    val SLACK = 5
    // 44 no error all 4400 rows are ok
    // 45 error but 3900 rows are only ok
    val DAYS = "1,120"
    val DAY_DURATION = 100
    val PIPELINE_NAME = "url-rep"
    override val REG_PARAM = 0.001
    override val SAMPLE_SIZE = 100
    override val PROFILE_NAME = "default"
    override val CONVERGENCE_TOL = 1E-6
    override val STEP_SIZE = 0.0001
    override val MINI_BATCH = 1.0
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
