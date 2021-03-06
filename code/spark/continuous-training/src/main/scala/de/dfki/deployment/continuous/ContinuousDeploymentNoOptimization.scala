package de.dfki.deployment.continuous

import de.dfki.core.sampling.Sampler
import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.experiments.Params
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class ContinuousDeploymentNoOptimization(val history: String,
                                         val streamBase: String,
                                         val evaluation: String = "prequential",
                                         val resultPath: String,
                                         val daysToProcess: Array[Int],
                                         slack: Int = 10,
                                         sampler: Sampler,
                                         otherParams: Params) extends Deployment(sampler) {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    // TODO: Should this be cached or not for no optimization deployment
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
    pipeline.model.setConvergenceTol(0.0)

    var time = 1

    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(pipeline, testData, resultPath, s"continuous-no-optimization-${sampler.name}")
    }
    while (!streamingSource.allFileProcessed()) {
      val start = System.currentTimeMillis()
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)

      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, s"continuous-no-optimization-${sampler.name}")
      }
      pipeline.updateTransformTrain(rdd)

      if (time % slack == 0) {
        val historicalSample = provideHistoricalSample(processedRDD)
        if (historicalSample.nonEmpty) {
          val trainingData = streamingContext.sparkContext.union(historicalSample)
          pipeline.updateTransformTrain(trainingData)
          if (evaluation != "prequential") {
            // if evaluation method is not prequential, only perform evaluation after a training step
            evaluateStream(pipeline, testData, resultPath, s"continuous-no-optimization-${sampler.name}")
          }
        } else {
          logger.warn(s"Sample in iteration $time is empty")
        }
      }
      decideToSavePipeline(pipeline, "continuous-no", otherParams, time)
      processedRDD += rdd
      time += 1
      val end = System.currentTimeMillis()
      val elapsed = end - start
      storeElapsedTime(elapsed, resultPath, s"continuous-no-optimization-${sampler.name}")
    }
  }
}
