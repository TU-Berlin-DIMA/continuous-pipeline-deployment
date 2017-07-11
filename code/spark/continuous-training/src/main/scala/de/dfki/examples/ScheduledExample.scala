package de.dfki.examples

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.preprocessing.parsers.CSVParser
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by bede01 on 28/12/16.
  */
object ScheduledExample {

  def main(args: Array[String]): Unit = {
    val streamingDataPath = "data/cover-types/stream-training/"
    val conf = new SparkConf().setMaster("local").setAppName("StreamingSVM")


    var ssc = new StreamingContext(conf, Seconds(1))
    var observations = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamingDataPath).map(_._2.toString).map(new CSVParser().parsePoint)
    val execService = Executors.newScheduledThreadPool(5)

    val task = new Runnable {
      def run() = {
        println("RESTARTING")
        ssc.stop(true, true)
        ssc = new StreamingContext(conf, Seconds(1))
        observations = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamingDataPath).map(_._2.toString).map(new CSVParser().parsePoint)
        observations.print()
        ssc.start()
        ssc.awaitTermination()


      }
    }
    // execService.scheduleAtFixedRate()
    execService.scheduleAtFixedRate(task, 10, 10, TimeUnit.SECONDS);
    observations.print()
    ssc.start()
    ssc.awaitTermination()
  }

}
