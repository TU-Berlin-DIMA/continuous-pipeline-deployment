package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class ContinuousDeploymentQualityAnalysis(val history: String,
                                          val stream: String,
                                          val evaluationPath: String,
                                          val resultPath: String,
                                          val samplingRate: Double = 0.1,
                                          val slack: Int = 10) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .cache()
    data.count()

    val testData = streamingContext.sparkContext.textFile(evaluationPath)
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, stream)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var time = 1

    evaluateStream(pipeline, testData, resultPath)

    while (!streamingSource.allFileProcessed()) {

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.cache()
      rdd.count()

      processedRDD += rdd

      if (time % slack == 0) {
        val data = historicalDataRDD(processedRDD, slack)
        pipeline.updateTransformTrain(data)
        evaluateStream(pipeline, testData, resultPath)
      }
      time += 1
    }
    processedRDD.foreach {
      r => r.unpersist(true)
    }
  }

  def historicalDataRDD(processedRDD: ListBuffer[RDD[String]], slack: Int) = {
    val now = processedRDD.size
    val history = now - slack
    processedRDD.slice(0, history)
      .reduce(_ union _)
      .sample(withReplacement = false, samplingRate)
      .union(processedRDD.slice(history, now).reduce(_ union _))
  }
}
