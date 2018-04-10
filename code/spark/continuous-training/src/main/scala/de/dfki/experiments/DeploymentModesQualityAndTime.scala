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
    override val RESULT_PATH = "../../../experiment-results/url-reputation/deployment-modes-quality-time"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/best/adam"
  }

  def main(args: Array[String]): Unit = {

    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Quality and Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
//
//    var ssc = new StreamingContext(conf, Seconds(1))
//
//    // online only training
//    var start = System.currentTimeMillis()
//    val onlinePipeline = getPipeline(ssc.sparkContext, params)
//    new OnlineDeployment(
//      streamBase = params.streamPath,
//      evaluation = s"${params.evaluationPath}",
//      resultPath = s"${params.resultPath}",
//      daysToProcess = params.days).deploy(ssc, onlinePipeline)
//    var end = System.currentTimeMillis()
//    storeTime(end - start, s"${params.resultPath}", "online-time")
//
//    ssc.stop(stopSparkContext = true, stopGracefully = true)
//    ssc = new StreamingContext(conf, Seconds(1))
//
//    // continuously train with full optimization set
//    start = System.currentTimeMillis()
//    val continuousPipelineWithOptimization = getPipeline(ssc.sparkContext, params)
//    new ContinuousDeploymentWithOptimizations(history = params.inputPath,
//      streamBase = params.streamPath,
//      evaluation = s"${params.evaluationPath}",
//      resultPath = s"${params.resultPath}",
//      daysToProcess = params.days,
//      slack = params.slack,
//      sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, continuousPipelineWithOptimization)
//    end = System.currentTimeMillis()
//    storeTime(end - start, s"${params.resultPath}", "continuous-with-optimization-time")
//
//
//    ssc.stop(stopSparkContext = true, stopGracefully = true)
//    ssc = new StreamingContext(conf, Seconds(1))
//
//    // baseline with no online learning
//    start = System.currentTimeMillis()
//    val baselinePipeline = getPipeline(ssc.sparkContext, params)
//    new BaselineDeployment(streamBase = params.streamPath,
//      evaluation = s"${params.evaluationPath}",
//      resultPath = s"${params.resultPath}",
//      daysToProcess = params.days
//    ).deploy(ssc, baselinePipeline)
//    end = System.currentTimeMillis()
//    storeTime(end - start, s"${params.resultPath}", "baseline-time")
//
//    ssc.stop(stopSparkContext = true, stopGracefully = true)
//    ssc = new StreamingContext(conf, Seconds(1))
//
//    // periodical with online learning
//    start = System.currentTimeMillis()
//    val periodicalPipeline = getPipeline(ssc.sparkContext, params)
//    new MultiOnlineDeployment(history = params.inputPath,
//      streamBase = params.streamPath,
//      evaluation = s"${params.evaluationPath}",
//      resultPath = s"${params.resultPath}",
//      daysToProcess = params.days,
//      frequency = params.dayDuration * 10,
//      sparkConf = conf
//    ).deploy(ssc, periodicalPipeline)
//    end = System.currentTimeMillis()
//    storeTime(end - start, s"${params.resultPath}", "periodical-time")
//
//    ssc.stop(stopSparkContext = true, stopGracefully = true)
    val ssc = new StreamingContext(conf, Seconds(1))

    // periodical with online learning
    val start = System.currentTimeMillis()
    val periodicalPipelineWarm = getPipeline(ssc.sparkContext, params)
    new MultiOnlineWithWarmStartingDeployment(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      frequency = params.dayDuration * 10,
      sparkConf = conf
    ).deploy(ssc, periodicalPipelineWarm)
    val end = System.currentTimeMillis()
    storeTime(end - start, s"${params.resultPath}", "periodical-warm-time")





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
