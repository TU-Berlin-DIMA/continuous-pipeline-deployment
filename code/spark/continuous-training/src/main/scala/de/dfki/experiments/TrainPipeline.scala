package de.dfki.experiments

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object TrainPipeline {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val PIPELINE_PATH = "data/criteo-full/pipelines/test-pipeline/pipeline"
  val RESULT_PATH = "data/criteo-full/pipelines/test-pipeline/quality"
  val DELIMITER = ","
  val NUM_FEATURES = 30000
  val NUM_ITERATIONS = 500

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", NUM_ITERATIONS)
    val pipelinePath = parser.get("pipeline", PIPELINE_PATH)
    val resultPath = parser.get("result", RESULT_PATH)

    val conf = new SparkConf().setAppName("Quality Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val spark = new SparkContext(conf)

    val data = spark.textFile(inputPath)

    val pipeline = if (Files.exists(Paths.get(pipelinePath))) {
      CriteoPipeline.loadFromDisk(pipelinePath, spark)
    } else {
      val t = getPipeline(spark, delimiter, numFeatures, numIterations, data)
      CriteoPipeline.saveToDisk(t, pipelinePath)
      t
    }

    val evaluationData = spark.textFile(evaluationPath)
    val results = pipeline.predict(evaluationData)

    val loss = LogisticLoss.logisticLoss(results)
    println(s"loss = $loss")

    val file = new File(s"$resultPath")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$loss\n")
    }
    finally fw.close()
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
