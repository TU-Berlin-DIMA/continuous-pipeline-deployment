package de.dfki.deployment

import de.dfki.core.sampling.Sampler
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
                                          val evaluation: String = "prequential",
                                          val resultPath: String,
                                          val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                          slack: Int = 10,
                                          sampler: Sampler) extends Deployment(slack, sampler) {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .setName("Historical data")
      .cache()
    data.count()

    val testData = streamingContext
      .sparkContext
      .textFile(evaluation)
      .setName("Evaluation Data set")
      .cache()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var time = 1

    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(pipeline, testData, resultPath, sampler.name)
    }
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream $time")
      rdd.cache()

      processedRDD += rdd
      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, sampler.name)
      }
      pipeline.updateTransformTrain(rdd)

      if (time % slack == 0) {
        val historicalSample = provideHistoricalSample(processedRDD, streamingContext.sparkContext)
        if (historicalSample.nonEmpty) {
          val trainingData = pipeline.transform(historicalSample.get)
          trainingData.cache()
          pipeline.train(trainingData)
          trainingData.unpersist()
          if (evaluation != "prequential") {
            // if evaluation method is not prequential, only perform evaluation after a training step
            evaluateStream(pipeline, testData, resultPath, sampler.name)
          }
        } else {
          logger.warn(s"Sample in iteration $time is empty")
        }
      }
      time += 1
    }
  }
}
