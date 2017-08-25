package de.dfki.utils

import de.dfki.core.streaming.BatchFileInputDStream
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
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }

  ignore("sortFiles") {
    val path = "data/url-reputation/stream-training/"
    val source = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, path)

    source.listFiles().foreach(println)
  }

  test("CurrentFolder") {
    val path = "data/criteo-full/processed/*"
    val source = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, path)

    print(source.currentFolder)
  }

  test("dummy2") {
    val v1 = "file:/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/processed/3/part-00090"
    val v2 = "file:/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/processed/4/part-00000"

    print("1" < "2")
  }

}
