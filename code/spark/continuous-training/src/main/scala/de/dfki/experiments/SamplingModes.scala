package de.dfki.experiments

import de.dfki.deployment.{ContinuousDeploymentSampleThenAppend, ContinuousDeploymentAppendThenSample}
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
object SamplingModes {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/day_0"
  val STREAM_PATH = "data/criteo-full/experiments/stream"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "../../../experiment-results/criteo-full/sampling-mode/local"
  val DELIMITER = ","
  val NUM_FEATURES = 3000000
  val NUM_ITERATIONS = 500
  val SLACK = 10


  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", NUM_ITERATIONS)
    val slack = parser.getInteger("slack", SLACK)

    val conf = new SparkConf().setAppName("Training Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val pipelineAppendThenSample = getPipeline(ssc.sparkContext, delimiter, numFeatures, numIterations, data)

    new ContinuousDeploymentAppendThenSample(history = inputPath,
          stream = s"$streamPath/*",
          eval = evaluationPath,
          resultPath = s"$resultPath/append-then-sample",
          samplingRate = 0.1,
          slack = slack).deploy(ssc, pipelineAppendThenSample)

    val pipelineSampleThenAppend = getPipeline(ssc.sparkContext, delimiter, numFeatures, numIterations, data)

    new ContinuousDeploymentSampleThenAppend(history = inputPath,
      stream = s"$streamPath/*",
      eval = evaluationPath,
      resultPath = s"$resultPath/sample-then-append",
      samplingRate = 0.1,
      slack = slack).deploy(ssc, pipelineSampleThenAppend)

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
