package de.dfki.utils

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class BatchFileInputDStreamTest extends FunSuite with BeforeAndAfterEach {

  var ssc: StreamingContext = _

  override def beforeEach() {
    val conf = new SparkConf()
      .setAppName("BatchFileInputDStreamTest")
      .setMaster("local[*]")
    ssc = new StreamingContext(conf, Seconds(1))
  }

  override def afterEach() {

  }

  test("sortFiles") {
    val path = "data/url-reputation/stream-training/"
    val source = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, path)

    source.listFiles().foreach(println)
  }

}
