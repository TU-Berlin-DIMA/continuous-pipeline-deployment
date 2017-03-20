package de.dfki.preprocessing.datasets

import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.feature.{Normalizer, StandardScaler}
import org.apache.spark.mllib.linalg.{DenseVector, Vector}
import org.apache.spark.sql.{DataFrame, SparkSession}

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
object CriteoPreprocessing {
  val INPUT_PATH = "data/criteo-sample/raw/"
  val OUTPUT_PATH = "data/criteo-sample/"
  val NUMBER_OF_STREAMING_FILES = 100


  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val data = parser.get("input-path", INPUT_PATH)
    val result = parser.get("output-path", OUTPUT_PATH)
    val fileCount = parser.getInteger("file-count", NUMBER_OF_STREAMING_FILES)

    val conf = new SparkConf().setAppName("Criteo Feature Engineering")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    val df = spark.read
      .format("csv")
      .option("delimiter", "\t")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(data)

    val numerical = getNumericalFeatures(df)

    val assembler = new VectorAssembler()
      .setInputCols(Array("_c1", "_c2", "_c3", "_c4", "_c5", "_c6",
        "_c7", "_c8", "_c9", "_c10", "_c11", "_c12", "_c13"))
      .setOutputCol("features")


    val vectorNumerical = assembler
      .transform(numerical)
      .select("_c0", "features")
      .rdd
      .map(r => (r.get(0), new DenseVector(r.getAs[org.apache.spark.ml.linalg.Vector](1).toArray)))

    //    val innerStruct =
    //      StructType(
    //        StructField("label", IntegerType) ::
    //          StructField("features", new VectorUDT()) :: Nil)
    //
    //    val d = spark.createDataFrame(vectorNumerical, innerStruct)

    val normalizedData = vectorNumerical.map(d => (d._1, new Normalizer().transform(d._2.asInstanceOf[Vector])))

    val scaler = new StandardScaler(withMean = true, withStd = true)
      .fit(normalizedData.map(_._2.asInstanceOf[Vector]))
    //      .setInputCol("features")
    //      .setOutputCol("features-transformed")
    //      .fit(d)

    val scaledData = normalizedData
      .map(d => (d._1, scaler.transform(d._2.asInstanceOf[Vector]).toArray.mkString(",")))


    val splits = scaledData.randomSplit(Array(0.1, 0.9))
    println(s"file-count = $fileCount")

    // shuffling the partitions are necessary since random split function internally sorts each partition
    splits(0)
      .map(row => row.toString.substring(1, row.toString.length - 1))
      .mapPartitions(partition => Random.shuffle(partition))
      .saveAsTextFile(s"$result/initial-training/")

    splits(1)
      .map(row => row.toString.substring(1, row.toString.length - 1))
      .repartition(fileCount)
      .mapPartitions(partition => Random.shuffle(partition))
      .saveAsTextFile(s"$result/stream-training/")
  }

  def getNumericalFeatures(df: DataFrame): DataFrame = {
    df.select("_c0", "_c1", "_c2", "_c3", "_c4", "_c5", "_c6", "_c7", "_c8", "_c9", "_c10", "_c11", "_c12", "_c13")
      .na.fill(0.0)
  }
}
