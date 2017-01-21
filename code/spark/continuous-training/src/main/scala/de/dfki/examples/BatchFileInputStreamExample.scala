package de.dfki.examples

import de.dfki.utils.BatchFileInputDStream
import de.dfki.utils.MLUtils.parsePoint
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by bede01 on 24/11/16.
  */
object BatchFileInputStreamExample {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local").setAppName("StreamingSVM")
    //val sc = new SparkContext(conf)
    val ssc = new StreamingContext(conf, Seconds(4))
    val input = "data/cover-types/stream-training/"
    val observations = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, input).map(_._2.toString).map(parsePoint).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
