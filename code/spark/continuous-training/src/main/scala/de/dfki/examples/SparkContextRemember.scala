package de.dfki.examples

import de.dfki.core.streaming.BatchFileInputDStream
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author bede01.
  */
object SparkContextRemember {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("Remember")
    //val sc = new SparkContext(conf)
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.remember(Seconds(10))

    val input = "data/sea/stream-training/"
    val observations = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, input)
    var i = 0
    val count = (rdd: RDD[String]) => {
      rdd.foreach(println)
    }

    observations.map(_._2.toString).foreachRDD(count)
    observations.pause()

    ssc.start()
    ssc.awaitTermination()

  }

}
