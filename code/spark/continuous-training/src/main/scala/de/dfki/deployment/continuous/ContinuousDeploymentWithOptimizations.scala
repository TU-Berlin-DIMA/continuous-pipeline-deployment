package de.dfki.deployment.continuous

import de.dfki.core.sampling.Sampler
import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * Continuous Deployment with Full Optimizations
  *
  * @author behrouz
  */
class ContinuousDeploymentWithOptimizations(val history: String,
                                            val streamBase: String,
                                            val evaluation: String = "prequential",
                                            val resultPath: String,
                                            val daysToProcess: Array[Int],
                                            slack: Int = 10,
                                            sampler: Sampler) extends Deployment(slack, sampler) {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // update and store update time
    val start = System.currentTimeMillis()
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .setName("Historical data")
      .cache()
    data.count()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[LabeledPoint]] = new ListBuffer[RDD[LabeledPoint]]()
    processedRDD += pipeline.transform(data)

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    pipeline.model.setConvergenceTol(0.0)

    var time = 1
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, s"${sampler.name}-with-optimization")
      }
      // update and transform using the pipeline and cache the materialized data
      val pRDD = pipeline.updateAndTransform(rdd).persist(StorageLevel.MEMORY_AND_DISK)
      pRDD.count()

      pipeline.train(pRDD)


      if (time % slack == 0) {
        val historicalSample = provideHistoricalSample(processedRDD, streamingContext.sparkContext)
        if (historicalSample.nonEmpty) {
          val transformed = historicalSample.get.repartition(streamingContext.sparkContext.defaultParallelism).cache()
          pipeline.train(transformed)
          transformed.unpersist(true)
        }
      }
      processedRDD += pRDD
      time += 1
    }

    val end = System.currentTimeMillis()
    val trainTime = end - start
    storeTrainingTimes(trainTime, s"$resultPath", "continuous-full-optimization-time")

    processedRDD.foreach { r => r.unpersist(true) }
  }

}
