package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

/**
  * no optimization
  * @author behrouz
  */
class ContinuousDeploymentNoOptimization(val history: String,
                                         val stream: String,
                                         val resultPath: String,
                                         val samplingRate: Double = 0.1,
                                         val slack: Long = 10) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)


    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, stream)

    def historicalDataRDD(recentItems: RDD[String]) = {
      streamingContext.sparkContext.textFile(streamingSource.getProcessedFiles.mkString(","))
        .union(data)
        .union(recentItems)
        .sample(withReplacement = false, samplingRate)
        .cache()
    }

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var proactiveRDD: RDD[String] = null
    var time = 0
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      //pipeline.update(rdd)

      if (proactiveRDD == null) {
        proactiveRDD = rdd
      } else {
        proactiveRDD = proactiveRDD.union(rdd)
      }
      if (time % slack == 0) {
        // train the pipeline
        val startTime = System.currentTimeMillis()
        val nextBatch = historicalDataRDD(proactiveRDD)
        // statistics update is done as part of the training
        pipeline.update(nextBatch)
        pipeline.train(nextBatch)
        val endTime = System.currentTimeMillis()

        // compute and store the training time
        val trainingTime = endTime - startTime
        storeTrainingTimes(trainingTime, resultPath)

        proactiveRDD = null
      }
      time += 1
    }
  }
}
