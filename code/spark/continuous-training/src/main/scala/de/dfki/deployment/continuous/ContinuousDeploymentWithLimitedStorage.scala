package de.dfki.deployment.continuous

import de.dfki.core.sampling.Sampler
import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.experiments.Params
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class ContinuousDeploymentWithLimitedStorage(val history: String,
                                             val streamBase: String,
                                           //  val materializeBase: String,
                                             val evaluation: String = "prequential",
                                             val resultPath: String,
                                             val daysToProcess: Array[Int],
                                             val materializedWindow: Int,
                                             slack: Int = 10,
                                             sampler: Sampler,
                                             otherParams: Params,
                                             online: Boolean = true) extends Deployment(sampler) {
  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .sample(withReplacement = false, 0.01)
      .setName("Historical data")
      .cache()
    data.count()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[LabeledPoint]] = new ListBuffer[RDD[LabeledPoint]]()
    val pData = pipeline.transform(data)
    // TODO: To make computation easier down the line we sample the historical data here
    processedRDD += pData.sample(withReplacement = false, 0.1)

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    pipeline.model.setConvergenceTol(0.0)

    var time = 1
    while (!streamingSource.allFileProcessed()) {
      val start = System.currentTimeMillis()

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString).persist(StorageLevel.MEMORY_ONLY)
      rdd.count()
      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, s"continuous-with-optimization-${sampler.name}")
      }
      // update and transform using the pipeline and cache the materialized data
      val pRDD = pipeline.updateAndTransform(rdd).setName(s"RDD_$time").persist(StorageLevel.MEMORY_ONLY)

      if (online) pipeline.train(pRDD)

      rdd.unpersist()

      if (time % slack == 0) {
        val historicalSample = provideHistoricalSample(processedRDD)
        if (historicalSample.nonEmpty) {
          // sparkcontext union preserves persistence, so we do not need to explicitly cache them again
          //val transformed = streamingContext.sparkContext.union(historicalSample).persist(StorageLevel.MEMORY_ONLY)
          //transformed.count()
          //val transformed = streamingContext.sparkContext.union(historicalSample)
          val transformedNotCached = streamingContext.sparkContext.union(historicalSample.filter(_.getStorageLevel != StorageLevel.MEMORY_ONLY))
          val transformedCached = streamingContext.sparkContext.union(historicalSample.filter(_.getStorageLevel == StorageLevel.MEMORY_ONLY))
          transformedNotCached.persist(StorageLevel.MEMORY_ONLY)
          transformedNotCached.count()

          pipeline.train(transformedCached.union(transformedNotCached))
          transformedNotCached.unpersist(blocking = false)
        } else {
          logger.warn(s"Sample in iteration $time is empty")
        }
      }
      decideToSavePipeline(pipeline, "continuous-with", otherParams, time)
      processedRDD += pRDD
      if (processedRDD.size > materializedWindow) {
        val evictIndex = processedRDD.size - 1 - materializedWindow
        processedRDD(evictIndex).unpersist(blocking = false)
      }
      time += 1
      val end = System.currentTimeMillis()
      val elapsed = end - start
      storeElapsedTime(elapsed, resultPath, s"continuous-with-optimization-${sampler.name}")

    }
  }
}
