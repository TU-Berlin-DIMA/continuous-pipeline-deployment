package de.dfki.preprocessing.datasets

import java.text.SimpleDateFormat

import de.dfki.utils.CommandLineParser
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


/**
  * @author behrouz
  */
object NYCTaxiPreprocessing {
  val INPUT_PATH = "data/nyc-taxi/raw"
  val OUTPUT_PATH = "data/nyc-taxi/processed"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("URL Data")
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
        val OUTPUT_FORMAT = new SimpleDateFormat("yyyyMMddHH")
        partition.flatMap {
          row =>
            try {
              val date = row.split(",")(1)
              val key = OUTPUT_FORMAT.format(INPUT_FORMAT.parse(date))
              Array((key, row))
            } catch {
              case ex: java.text.ParseException => {
                Array[(String,String)]()
              }
            }
        }
      }
    }.toDF("day","content")


    groupedRDD.repartition($"day").write.partitionBy("day").text(outputPath)
  }

//  def getName(year: Int, month: Int) = f"yellow_tripdata_$year-$month%02d.csv"
}
