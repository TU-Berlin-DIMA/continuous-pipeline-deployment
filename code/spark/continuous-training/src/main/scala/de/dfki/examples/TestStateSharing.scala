package de.dfki.examples

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{ConstantInputDStream, DStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by bede01 on 05/01/17.
  */
object TestStateSharing {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Test Application").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(1))
    val rdd = ssc.sparkContext.parallelize(List(1, 2, 3, 4, 5))
    val stream1 = new ConstantInputDStream(ssc, rdd)
    val stream2 = new ConstantInputDStream(ssc, rdd)

    val obj = new StaticObject()
    obj.use(stream2).print()
    obj.modify(stream1)


    ssc.start()
    ssc.awaitTermination()

    ssc.stop()

  }

  class StaticObject extends Serializable {
    var weights = List(2, 3, 4, 5)

    def modify(stream: DStream[Int]): Unit = {
      stream.foreachRDD { (rdd, time) =>
        val sum = rdd.sum()
        weights = weights.map(a => a + sum.toInt)
      }
    }

    def use(stream: DStream[Int]): DStream[Int] = {
      stream.map(vals => vals + weights.sum)
    }
  }

}
