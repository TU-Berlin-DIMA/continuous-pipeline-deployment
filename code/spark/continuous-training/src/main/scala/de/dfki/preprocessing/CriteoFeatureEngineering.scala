package de.dfki.preprocessing

import de.dfki.utils.CommandLineParser
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.util.Random


/**
  * ------ Display Advertising Challenge ------
  *
  * Dataset: dac-v2
  *
  * This dataset contains feature values and click feedback for millions of display
  * ads. Its purpose is to benchmark algorithms for clickthrough rate (CTR) prediction.
  * It is a sample of the dataset used for the Display Advertising Challenge hosted by Kaggle:
  * https://www.kaggle.com/c/criteo-display-ad-challenge/
  *
  * Dataset construction:
  *
  * The training dataset consists of a portion of Criteo's traffic.
  * Each row corresponds to a display ad served by Criteo and the first
  * column is indicates whether this ad has been clicked or not.
  * The positive (clicked) and negatives (non-clicked) examples have both been
  * subsampled (but at different rates) in order to reduce the dataset size.
  * *
  * There are 13 features taking integer values (mostly count features) and 26
  * categorical features. The values of the categorical features have been hashed
  * onto 32 bits for anonymization purposes.
  * The semantic of these features is undisclosed. Some features may have missing values.
  *
  * The rows are chronologically ordered.
  *
  * ====================================================
  *
  * Format:
  *
  * The columns are tab separeted with the following schema:
  * <label> <integer feature 1> ... <integer feature 13> <categorical feature 1> ... <categorical feature 26>
  *
  * When a value is missing, the field is just empty.
  *
  * ====================================================
  *
  * Dataset assembled by Olivier Chapelle (o.chapelle@criteo.com)
  *
  * @author bede01.
  */
object CriteoFeatureEngineering {
  val INPUT_PATH = "data/criteo-sample/raw/"
  val OUTPUT_PATH = "data/criteo-sample/processed/"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val data = parser.get("input-path", INPUT_PATH)
    val result = parser.get("output-path", OUTPUT_PATH)

    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("Criteo Feature Engineering")
      .getOrCreate()

    val df = spark.read.format("csv").option("delimiter", "\t").option("header", "false").load(data)

    val numerical = getNumericalFeatures(df)
    val splits = numerical.randomSplit(Array(0.1, 0.9))
    implicit val myObjEncoder = org.apache.spark.sql.Encoders.kryo[Row]

    // shuffling the partitions are necessary since random split function internally sorts each partition
    splits(0).rdd
      .map(row => row.toString.substring(1, row.toString.length - 1))
      .mapPartitions(partition => Random.shuffle(partition))
      .saveAsTextFile(s"$result/initial-training/")

    splits(1).rdd
      .map(row => row.toString.substring(1, row.toString.length - 1))
      .repartition(500)
      .mapPartitions(partition => Random.shuffle(partition))
      .saveAsTextFile(s"$result/stream-training/")
  }

  def getNumericalFeatures(df: DataFrame): DataFrame = {
    df.select("_c0", "_c1", "_c2", "_c3", "_c4", "_c5", "_c6", "_c7", "_c8", "_c9", "_c10", "_c11", "_c12", "_c13")
      .na.fill("0")
  }

  def trialIterator(row: Iterator[Row]): Iterator[Row] = {
    row
  }
}
