package de.dfki.experiments

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.continuous.{ContinuousDeploymentNoOptimization, ContinuousDeploymentWithOptimizations}
import de.dfki.experiments.profiles.URLProfile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * code for effect of optimizations in the entire and individual pipeline comonents
  *
  * @author behrouz
  */
object OptimizationTimes extends Experiment {
  override val defaultProfile = new URLProfile()

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Optimization Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    var ssc = new StreamingContext(conf, Seconds(1))

    // Continuous deployment without any optimizations
    var start = System.currentTimeMillis()
    val continuousPipeline = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentNoOptimization(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, continuousPipeline)
    var end = System.currentTimeMillis()
    storeTime(end - start, s"${params.resultPath}", "continuous-no-optimization-time")

    // restart the context
    ssc.stop(stopSparkContext = true, stopGracefully = true)
    ssc = new StreamingContext(conf, Seconds(1))


    //  Continuous deployment with all the optimizations
    start = System.currentTimeMillis()
    val continuousPipelineWithOptimization = getPipeline(ssc.sparkContext, params)
    new ContinuousDeploymentWithOptimizations(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = s"${params.evaluationPath}",
      resultPath = s"${params.resultPath}",
      daysToProcess = params.days,
      slack = params.slack,
      sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, continuousPipelineWithOptimization)
    end = System.currentTimeMillis()
    storeTime(end - start, s"${params.resultPath}", "continuous-with-optimization")


  }
}
