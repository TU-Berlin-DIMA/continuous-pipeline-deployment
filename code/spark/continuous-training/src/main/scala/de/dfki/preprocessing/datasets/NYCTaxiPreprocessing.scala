package de.dfki.preprocessing.datasets

import java.text.SimpleDateFormat
import java.util.Calendar

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * @author behrouz
  */
object NYCTaxiPreprocessing {
  val INPUT_PATH = "data/nyc-taxi/raw"
  val OUTPUT_PATH = "data/nyc-taxi/processed"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("NYC Taxi Data Preprocessing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val spark = new SparkContext(conf)
    val inputPath = parser.get("input-path", INPUT_PATH)
    val outputPath = parser.get("output-path", OUTPUT_PATH)

    //  val fileName = getName(2015, 1)
    val sqlContext = new SQLContext(spark)
    import sqlContext.implicits._

    val rdd = spark.textFile(s"$inputPath")

    val groupedRDD = rdd.mapPartitions {
      partition => {
        val INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val cal = Calendar.getInstance()
        // for generating unique day numbers
        val DAYS_IN_2015 = 365
        partition.flatMap {
          row =>
            try {
              val date = row.split(",")(1)
              val parsedDate = INPUT_FORMAT.parse(date)
              cal.setTime(parsedDate)
              val hour = cal.get(Calendar.HOUR_OF_DAY)
              val day = if (cal.get(Calendar.YEAR) > 2015)
                DAYS_IN_2015 + cal.get(Calendar.DAY_OF_YEAR)
              else
                cal.get(Calendar.DAY_OF_YEAR)
              Array((day, hour, row))
            } catch {
              case ex: java.text.ParseException => {
                Array[(Int, Int, String)]()
              }
            }
        }
      }
    }.toDF("day", "hour", "content")


    groupedRDD.repartition($"day", $"hour").write.partitionBy("day", "hour").text(outputPath)
  }
}

object NYCTaxiCorrectness {
  val STREAM_PATH = "data/nyc-taxi/processed/stream"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("NYC Taxi Data Preprocessing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val ssc = new StreamingContext(conf, Seconds(1))
    val streamPath = parser.get("stream-path", STREAM_PATH)
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamPath, days = (1 to 731).toArray)

    //val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
    streamingSource.files.foreach(println)
    //rdd.collect.foreach(println)

  }
}
