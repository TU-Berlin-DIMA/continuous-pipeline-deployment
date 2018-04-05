package de.dfki.experiments

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.baseline.BaselineDeployment
import de.dfki.deployment.continuous.ContinuousDeploymentNoOptimization
import de.dfki.deployment.online.OnlineDeployment
import de.dfki.deployment.periodical.PeriodicalDeployment
import de.dfki.experiments.profiles.URLProfile
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object DeploymentModesQuality extends Experiment {

  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "../../../experiment-results/url-reputation/deployment-modes"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/best/adam"
  }

  def main(args: Array[String]): Unit = {

    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Quality Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))

    // continuously trained with a uniform sample of the historical data
    val onlinePipeline = getPipeline(ssc.sparkContext, params)
    new OnlineDeployment(
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days).deploy(ssc, onlinePipeline)

    // continuously trained with a time based sample of the historical data
    val continuousPipeline = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentNoOptimization(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, continuousPipeline)

    // baseline with no online learning
    val baselinePipeline = getPipeline(ssc.sparkContext, params)
    new BaselineDeployment(streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days
    ).deploy(ssc, baselinePipeline)

    // periodical with online learning
    val periodicalPipeline = getPipeline(ssc.sparkContext, params)
    new PeriodicalDeployment(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      frequency = params.dayDuration * 10
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
