package de.dfki.experiments

import java.io.{File, FileWriter}

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.criteo.{InputParser, OneHotEncoder}
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.log4j.Logger
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object FeatureDiscovery {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"
  val STREAM_PATH = "data/criteo-full/experiments/stream/*"
  val RESULT_PATH = "../../../experiment-results/criteo-full/feature-discovery/counts.txt"
  val NUM_FEATURES = 3000000
  val DELIMITER = "\t"

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val streamPath = parser.get("stream", STREAM_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val data = ssc.sparkContext.textFile(inputPath)

    val inputParser = new InputParser(delim = delimiter)
    val oneHotEncoder = new OneHotEncoder(numFeatures)

    val processed = inputParser.transform(ssc.sparkContext, data)
    oneHotEncoder.update(ssc.sparkContext, processed)
    val file = new File(s"$resultPath")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"${oneHotEncoder.approximateFeatureSize}\n")
    }
    finally {
      fw.close()
    }
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamPath)

    streamingSource
      .map(_._2.toString)
      .transform(rdd => inputParser.transform(ssc.sparkContext, rdd))
      .transform(rdd => {
        oneHotEncoder.update(ssc.sparkContext, rdd)
        val file = new File(s"$resultPath")
        file.getParentFile.mkdirs()
        val fw = new FileWriter(file, true)
        try {
          fw.write(s"${oneHotEncoder.approximateFeatureSize}\n")
        }
        finally {
          fw.close()
        }
        rdd
      })
      .foreachRDD(a => a)

    ssc.start()
    ssc.awaitTermination()

  }
}

object FeatureDiscoveryDayByDay {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0," +
    "data/criteo-full/experiments/stream/1," +
    "data/criteo-full/experiments/stream/2," +
    "data/criteo-full/experiments/stream/3," +
    "data/criteo-full/experiments/stream/4," +
    "data/criteo-full/experiments/stream/5"
  val RESULT_PATH = "../../../experiment-results/criteo-full/feature-discovery-local/counts.txt"
  val NUM_FEATURES = 300000000
  val DELIMITER = ","

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)
    val resultPath = parser.get("result", RESULT_PATH)
    val delimiter = parser.get("delimiter", DELIMITER)
    val numFeatures = parser.getInteger("features", NUM_FEATURES)

    val conf = new SparkConf().setAppName("FeatureDiscovery Day By Day")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)


    val inputParser = new InputParser(delim = delimiter)
    val oneHotEncoder = new OneHotEncoder(numFeatures)

    inputPath.split(",").foreach {
      in =>
        val data = spark.textFile(in)
        val processed = inputParser.transform(spark, data)
        oneHotEncoder.update(spark, processed)
        val file = new File(s"$resultPath")
        file.getParentFile.mkdirs()
        val fw = new FileWriter(file, true)
        try {
          fw.write(s"${oneHotEncoder.approximateFeatureSize}\n")
        }
        finally {
          fw.close()
        }
    }

  }
}

object FeatureCountExact {
  val INPUT_PATH = "data/criteo-full/experiments/initial-training/0"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val inputPath = parser.get("input", INPUT_PATH)

    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = new SparkContext(conf)

    val inputParser = new InputParser(",")
    val raw = spark.textFile(inputPath)
    val data = inputParser.transform(spark, raw)

    val exactCount = data.map(_.categorical).flatMap(a => a).distinct().count()
    println(s"Exact Feature Count: $exactCount")
  }
}
