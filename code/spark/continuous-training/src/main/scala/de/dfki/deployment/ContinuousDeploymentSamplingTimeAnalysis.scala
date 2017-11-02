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
                                               val windowSize: Int = -1,
                                               val iter: Int = 50) extends Deployment {

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)

    val factor = if (windowSize == -1) 2 else 1
    for (i <- 0 to (factor * dayDuration)) {
      val next = streamingSource.generateNextRDD().get.map(_._2.toString)
      if (i == 0) {
        pipeline.update(next)
      }
      next.setName(s"Stream Day1-$i")
      processedRDD += next
    }

    val start = System.currentTimeMillis()
    var processTime = 0L
    var time = 1
    while (time < iter) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream Day 2-$time")
      val rand = new Random(System.currentTimeMillis())
      val nextBatch = if (samplingRate == 0.0) {
        logger.info("Sampling rate is 0.0")
        val s = System.currentTimeMillis()
        rdd.setName("Next Batch").cache().count()
        val e = System.currentTimeMillis()
        processTime += (e - s)
        rdd
      } else if (windowSize == -1) {
        logger.info("Entire history")
        val lists = processedRDD.filter(a => rand.nextDouble() < samplingRate).toList
        logger.info(s"returning ${lists.size} out of ${processedRDD.size}")
        val r = streamingContext.sparkContext.union(rdd :: lists).setName("Next Batch").cache()
        val s = System.currentTimeMillis()
        r.count()
        val e = System.currentTimeMillis()
        processTime += (e - s)
        r
      } else {
        val now = processedRDD.size
        val history = processedRDD.slice(now - windowSize, now).filter(a => rand.nextDouble() < samplingRate).toList
        logger.info(s"returning ${history.size} out of ${processedRDD.size}")
        val r = streamingContext.sparkContext.union(rdd :: history).setName("Next Batch").cache()
        val s = System.currentTimeMillis()
        r.count()
        val e = System.currentTimeMillis()
        processTime += (e - s)
        r
      }
      nextBatch.unpersist(true)
      processedRDD += rdd
      time += 1
    }
    val end = System.currentTimeMillis()
    val trainTime = end - start
    if (samplingRate == 0.0) {
      storeTrainingTimes(trainTime, s"$resultPath/no-sampling/time")
      storeTrainingTimes(processTime, s"$resultPath/no-sampling/data-processing")
    } else if (windowSize == -1) {
      storeTrainingTimes(trainTime, s"$resultPath/entire-history/time")
      storeTrainingTimes(processTime, s"$resultPath/entire-history/data-processing")
    } else {
      storeTrainingTimes(trainTime, s"$resultPath/$windowSize/time")
      storeTrainingTimes(processTime, s"$resultPath/$windowSize/data-processing")
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

