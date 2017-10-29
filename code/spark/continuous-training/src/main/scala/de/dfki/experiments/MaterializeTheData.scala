package de.dfki.experiments

import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.preprocessing.parsers.CustomVectorParser
import de.dfki.utils.CommandLineParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object MaterializeTheData {
  val INPUT_PATH = "data/criteo-full/experiments/stream/day_5"
  val MATERIALIZED_PATH = "data/criteo-full/experiments/materialized/stream/day_5"
  val DELIMITER = ","
  val NUM_FEATURES = 30000

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val materializedPath = parser.get("materialize", MATERIALIZED_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)

    val conf = new SparkConf().setAppName("Training Time Experiment")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)
    val data = spark.textFile(inputPath)

    val pipeline = new CriteoPipeline(spark = spark, numCategories = numFeatures, delim = delimiter)
    pipeline.update(data)
    val materialized = pipeline.transform(data)
    val dataParser = new CustomVectorParser()
    materialized.map(dataParser.unparsePoint).saveAsTextFile(materializedPath)
  }


}
