package de.dfki.experiments.misc

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithAdam
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.utils.CommandLineParser
import org.apache.log4j.Logger
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object TrainPipeline {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val EVALUATION_PATH = "data/criteo-full/experiments/evaluation/6"
  val RESULT_PATH = "data/criteo-full/pipelines/test-pipeline"
  val DELIMITER = ","
  val NUM_FEATURES = 30000
  val NUM_ITERATIONS = 1000
  val INCREMENTS = 200
  val STEP_SIZE = 0.1
  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val evaluationPath = parser.get("evaluation", EVALUATION_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)
    val numIterations = parser.getInteger("iterations", NUM_ITERATIONS)
    val increment = parser.getInteger("increment", INCREMENTS)
    val resultPath = parser.get("result", RESULT_PATH)
    val stepSize = parser.getDouble("step", STEP_SIZE)

    val conf = new SparkConf().setAppName("Train Pipeline")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val spark = new SparkContext(conf)

    val data = spark.textFile(inputPath)

    var pipeline = new CriteoPipeline(spark,
      delim = delimiter,
      updater = new SquaredL2UpdaterWithAdam(),
      miniBatchFraction = 0.1,
      stepSize = stepSize,
      numIterations = increment,
      numCategories = numFeatures)

    val transformed = pipeline.updateAndTransform(data)
    transformed.setName("Training Data set")
    transformed.persist(StorageLevel.MEMORY_ONLY)
    val evaluationData = spark.textFile(evaluationPath).setName("Evaluation Data Set").cache()


    var i = increment
    while (i <= numIterations) {
      if (Files.exists(Paths.get(s"$resultPath/pipeline_$i"))) {
        logger.info(s"Pipeline '$resultPath/pipeline_$i'  exists !!!")
        pipeline = CriteoPipeline.loadFromDisk(s"$resultPath/pipeline_$i", spark)
        i += increment
      } else {
        logger.info(s"Pipeline '$resultPath/pipeline_$i'  Does not exists!!!")

        pipeline.train(transformed)
        val results = pipeline.predict(evaluationData)
        val loss = LogisticLoss.fromRDD(results)
        println(s"loss_$i = $loss")
        val file = new File(s"$resultPath/quality")
        file.getParentFile.mkdirs()
        val fw = new FileWriter(file, true)
        try {
          fw.write(s"$i\t$loss\n")
        }
        finally fw.close()
        CriteoPipeline.saveToDisk(pipeline, s"$resultPath/pipeline_$i")
        i += increment
      }
    }

  }


}
