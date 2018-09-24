package de.dfki.experiments

import de.dfki.core.sampling.Sampler
import de.dfki.deployment.continuous.ContinuousDeploymentWithLimitedStorage
import de.dfki.experiments.profiles.URLProfile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object RunContinuousDeployment extends Experiment {
  override val defaultProfile = new URLProfile()

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Run Continuous Deployment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val ssc = new StreamingContext(conf, Seconds(1))
    val pipeline = getPipeline(ssc.sparkContext, params)
    val sampler = Sampler.getSampler(params.samplingStrategy, params.sampleSize, params.rollingWindow)
    new ContinuousDeploymentWithLimitedStorage(history = params.inputPath,
      streamBase = params.streamPath,
      evaluation = params.evaluationPath,
      resultPath = params.resultPath,
      daysToProcess = params.days,
      materializedWindow = params.materializedWindow,
      slack = params.slack,
      sampler = sampler,
      otherParams = params).deploy(ssc, pipeline)
  }

}
