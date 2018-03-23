package de.dfki.experiments

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.{ContinuousDeploymentQualityAnalysis, OnlineDeploymentQualityAnalysis, PeriodicalDeploymentQualityAnalysis}
import de.dfki.experiments.profiles.Profile
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object DeploymentModesQuality extends Experiment {

  object DefaultProfile extends Profile {
    val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
    val STREAM_PATH = "data/url-reputation/processed/stream"
    val EVALUATION_PATH = "prequential"
    val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
    val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-3000"
    val DELIMITER = ","
    // URL FEATURE SIZE
    // val NUM_FEATURES = 3231961
    val NUM_FEATURES = 3000
    val NUM_ITERATIONS = 20000
    val SLACK = 5
    // 44 no error all 4400 rows are ok
    // 45 error but 3900 rows are only ok
    val DAYS = "1,120"
    val SAMPLING_RATE = 0.1
    val DAY_DURATION = 100
    val PIPELINE_NAME = "url-rep"
    val REG_PARAM = 0.001
    override val SAMPLE_SIZE = 100
    override val PROFILE_NAME = "default"
    override val CONVERGENCE_TOL = 1E-6
    override val STEP_SIZE = 0.001
    override val MINI_BATCH = 0.1
  }

  def main(args: Array[String]): Unit = {

    val params = getParams(args, DefaultProfile)

    val conf = new SparkConf().setAppName("Quality Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(params.inputPath)

    // continuously trained with a uniform sample of the historical data
    val onlinePipeline = getPipeline(ssc.sparkContext, params)

    new OnlineDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}/continuous",
      daysToProcess = params.days).deploy(ssc, onlinePipeline)

    // continuously trained with a time based sample of the historical data
    val continuousPipeline = getPipeline(ssc.sparkContext, params)

    new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}/continuous",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.dayDuration)).deploy(ssc, continuousPipeline)

    val periodicalPipeline = getPipeline(ssc.sparkContext, params)

    new PeriodicalDeploymentQualityAnalysis(history = params.inputPath,
      streamBase = params.streamPath,
      evaluationPath = s"$params.evaluationPath",
      resultPath = s"$params.resultPath/periodical",
      numIterations = params.numIterations,
      daysToProcess = params.days
    ).deploy(ssc, periodicalPipeline)

  }

  def getPipeline(spark: SparkContext, delimiter: String, numFeatures: Int, numIterations: Int, data: RDD[String]) = {
    val pipeline = new CriteoPipeline(spark,
      delim = delimiter,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 0.1,
      numIterations = numIterations,
      numCategories = numFeatures)
    pipeline.updateTransformTrain(data)
    pipeline
  }


}
