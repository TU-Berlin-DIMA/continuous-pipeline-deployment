package de.dfki.examples

import de.dfki.utils.BatchFileInputDStream
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming._

/**
  * Created by bede01 on 16/01/17.
  */
object SimpleCheckpoint {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Checkpoint Example")
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(1))

    ssc.checkpoint(".")
    val data = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, "data/word-count/").map(_._2.toString)

    def mappingFunc(batchTime: Time, key: String, value: Option[Long], state: State[Long]): Option[Long] = {
      val sum = value.getOrElse(0L).toLong + state.getOption.getOrElse(0L)
      state.update(sum)
      Some(sum)
    }

    data
      .flatMap(w => w.split("[ \\f\\t\\v\\n]"))
      .map(w => ("d", 1L))
      .mapWithState(StateSpec.function(mappingFunc _))
      .print
    //data.print()

    ssc.start()
    ssc.awaitTermination()
  }

}
