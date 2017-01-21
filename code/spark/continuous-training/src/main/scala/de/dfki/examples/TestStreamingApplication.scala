package de.dfki.examples

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by bede01 on 23/11/16.
  */
object TestStreamingApplication {

  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("Streaming Test Application")
    val ssc = new StreamingContext(conf, Seconds(5))
    val data = ssc.textFileStream("file:///Users/bede01/Documents/work/phd/continuous-training-serving/code/spark/continuous-training/data/test/")
    data.print()
    ssc.start()
  }
}
