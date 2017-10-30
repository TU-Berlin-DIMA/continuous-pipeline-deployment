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
                                          val streamBase: String,
                                          val evaluationPath: String,
                                          val resultPath: String,
                                          val samplingRate: Double = 0.1,
                                          val slack: Int = 10,
                                          val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                          val windowSize: Int = -1) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .setName("Historical data")
      .cache()
    data.count()

    val testData = streamingContext.sparkContext.textFile(evaluationPath).setName("Evaluation Data set").cache()
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var time = 1

    evaluateStream(pipeline, testData, resultPath, windowSize.toString)

    while (!streamingSource.allFileProcessed()) {

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream $time")
      rdd.cache()

      processedRDD += rdd
      if (time % slack == 0) {
        pipeline.update(streamingContext.sparkContext.union(processedRDD.slice(processedRDD.size - slack, processedRDD.size)))
        val data = historicalDataRDD(processedRDD, samplingRate, slack, streamingContext.sparkContext, windowSize)
        val trainingData = pipeline.transform(data)
        trainingData.cache()
        pipeline.train(trainingData)
        trainingData.unpersist()
        evaluateStream(pipeline, testData, resultPath, windowSize.toString)
      }
      time += 1
      if (time > windowSize && windowSize != -1) {
        processedRDD(time - windowSize).unpersist(blocking = true)
      }
    }
    processedRDD.foreach {
      r => r.unpersist(true)
    }
  }
}
