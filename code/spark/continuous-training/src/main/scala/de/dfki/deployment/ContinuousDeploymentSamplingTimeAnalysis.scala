package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * @author behrouz
  */
class ContinuousDeploymentSamplingTimeAnalysis(val history: String,
                                               val streamBase: String,
                                               val resultPath: String,
                                               val samplingRate: Double = 0.1,
                                               val dayDuration: Int = 100,
                                               val daysToProcess: Array[Int] = Array(1, 2),
                                               val slack: Int = 10,
                                               val windowSize: Int = -1) extends Deployment {

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with


    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)

    for (i <- 0 to dayDuration) {
      val next = streamingSource.generateNextRDD().get.map(_._2.toString)
      if (i == 0) {
        pipeline.update(next)
      }
      next.setName(s"Stream Day1-$i")
    }

    val start = System.currentTimeMillis()
    var time = 1
    while (time < 5) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream Day 2-$time")
      val rand = new Random(System.currentTimeMillis())

      val nextBatch = if (samplingRate == 0.0) {
        logger.info("Sampling rate is 0.0")
        rdd
      } else if (windowSize == -1) {
        logger.info("Entire history")
        streamingContext.sparkContext.union(rdd :: processedRDD.filter(a => rand.nextDouble() < samplingRate).toList)
      } else {
        val now = processedRDD.size
        val history = processedRDD.slice(now - dayDuration, now).filter(a => rand.nextDouble() < samplingRate)
        streamingContext.sparkContext.union(rdd :: history.toList)
      }
      val transformed = pipeline.transform(nextBatch)
      pipeline.train(transformed)
      processedRDD += rdd
      time += 1
    }
    val end = System.currentTimeMillis()
    val trainTime = end - start
    if (samplingRate == 0.0) {
      storeTrainingTimes(trainTime, s"$resultPath/no-sampling")
    } else if (windowSize == -1) {
      storeTrainingTimes(trainTime, s"$resultPath/entire-history")
    } else {
      storeTrainingTimes(trainTime, s"$resultPath/$windowSize")
    }
    processedRDD.foreach(_.unpersist(true))
  }
  def storeTrainingTimes(time: Long, root: String) = {
    val file = new File(s"$root")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$time\n")
    }
    finally fw.close()
  }

}

