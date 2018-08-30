package de.dfki.experiments

import de.dfki.core.sampling.{TimeBasedSampler, UniformSampler, WindowBasedSampler}
import de.dfki.deployment.continuous.ContinuousDeploymentWithLimitedStorage
import de.dfki.experiments.profiles.URLProfile
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object DynamicMaterialization extends Experiment {
  override val defaultProfile = new URLProfile()

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)

    val conf = new SparkConf().setAppName("Optimization Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val totalNumberOfChunks = params.dayDuration * params.days.length
    val rollingWindowSize = (0.5 * totalNumberOfChunks).toInt
    val rates = List(0.0, 0.2, 0.6, 1.0)
    println(s"Total number of Chunks = $totalNumberOfChunks")
    rates.foreach {
      r =>
        val materializedWindowSize = (r * totalNumberOfChunks).toInt
        println(s"$materializedWindowSize/$totalNumberOfChunks are materialized")
        val ssc = new StreamingContext(conf, Seconds(1))
        val pipeline = getPipeline(ssc.sparkContext, params)
        new ContinuousDeploymentWithLimitedStorage(history = params.inputPath,
          streamBase = params.streamPath,
          evaluation = s"${params.evaluationPath}",
          resultPath = s"${params.resultPath}/rate=$r",
          daysToProcess = params.days,
          materializedWindow = materializedWindowSize,
          slack = params.slack,
          sampler = new TimeBasedSampler(size = params.sampleSize),
          otherParams = params).deploy(ssc, pipeline)
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    }


    rates.filter(_ != 0.0).foreach {
      r =>
        val materializedWindowSize = (r * totalNumberOfChunks).toInt
        println(s"$materializedWindowSize/$totalNumberOfChunks are materialized")
        val ssc = new StreamingContext(conf, Seconds(1))
        val pipeline = getPipeline(ssc.sparkContext, params)
        new ContinuousDeploymentWithLimitedStorage(history = params.inputPath,
          streamBase = params.streamPath,
          evaluation = s"${params.evaluationPath}",
          resultPath = s"${params.resultPath}/rate=$r",
          daysToProcess = params.days,
          materializedWindow = materializedWindowSize,
          slack = params.slack,
          sampler = new UniformSampler(size = params.sampleSize),
          otherParams = params).deploy(ssc, pipeline)
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    }

    rates.foreach {
      r =>
        val materializedWindowSize = (r * totalNumberOfChunks).toInt
        println(s"$materializedWindowSize/$materializedWindowSize/$totalNumberOfChunks are materialized")
        val ssc = new StreamingContext(conf, Seconds(1))
        val pipeline = getPipeline(ssc.sparkContext, params)
        new ContinuousDeploymentWithLimitedStorage(history = params.inputPath,
          streamBase = params.streamPath,
          evaluation = s"${params.evaluationPath}",
          resultPath = s"${params.resultPath}/rate=$r",
          daysToProcess = params.days,
          materializedWindow = materializedWindowSize,
          slack = params.slack,
          sampler = new WindowBasedSampler(size = params.sampleSize, window = rollingWindowSize),
          otherParams = params).deploy(ssc, pipeline)
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    }

  }

}
