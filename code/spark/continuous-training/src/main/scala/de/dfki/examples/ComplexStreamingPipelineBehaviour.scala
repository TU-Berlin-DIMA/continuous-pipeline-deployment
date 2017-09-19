package de.dfki.examples

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

/**
  * @author behrouz
  */
object ComplexStreamingPipelineBehaviour {
  val BATCH_INPUT = "data/test/batch-input"
  val STREAM_INPUT = "data/test/stream-input"
  val TEMP_INPUT = "data/test/temp-input"
  val MICRO_DURATION = 1
  val SLACK = 4
  @transient val logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)


    val batchPath = parser.get("batch-path", BATCH_INPUT)
    val streamPath = parser.get("stream-path", STREAM_INPUT)
    val miniBatchDuration = parser.getLong("micro-duration", MICRO_DURATION)
    val sgdSlack = parser.getLong("slack", SLACK)

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val experimentId = dateFormat.format(Calendar.getInstance().getTime)
    val tempPath = s"${parser.get("temp-input", TEMP_INPUT)}/$experimentId"


    new File(s"$tempPath").mkdirs()
    val ssc = new StreamingContext(conf, Seconds(miniBatchDuration))

    def batch = ssc.sparkContext.textFile(s"$batchPath,$tempPath")

    val stream = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamPath)
      .map(_._2.toString)


    val storeRDD = (rdd: RDD[String], time: Time) => {
      val hadoopConf = new Configuration()
      hadoopConf.set("mapreduce.output.basename", time.toString())
      rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$tempPath", classOf[NullWritable], classOf[String],
        classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
    }

    def print1(obj: Object, rdd: RDD[String]): RDD[String] = {
      println(s"PRINT 1: ${obj.a}")
      obj.a = obj.a + 1
      //rdd.collect.foreach(println)
      rdd
    }


    def print2(obj: Object, rdd: RDD[String]): RDD[String] = {
      println(s"PRINT 2: ${obj.a}")
      obj.a = 0
      //logger.info(fast.union(history).collect.toList)
      rdd
    }

    val obj: Object = new Object()

    stream
      // train on the stream
      .transform(rdd => print1(obj, rdd))
      // create a window
      .window(Seconds(sgdSlack), Seconds(sgdSlack))
      // train on the batch and stream
      .transform(rdd => print2(obj, rdd))
      // write to disk
      .count()
      .print()


    ssc.start()
    ssc.awaitTermination()

  }

  class Object {
    var a = 0
  }

}
