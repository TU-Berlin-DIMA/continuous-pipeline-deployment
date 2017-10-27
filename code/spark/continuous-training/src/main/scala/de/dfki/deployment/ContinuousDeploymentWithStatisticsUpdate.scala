package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

/**
  * Only statistics update
  *
  * @author behrouz
  */
class ContinuousDeploymentWithStatisticsUpdate(val history: String,
                                               val stream: String,
                                               val eval: String,
                                               val resultPath: String,
                                               val samplingRate: Double = 0.1,
                                               val slack: Long = 10) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .repartition(streamingContext.sparkContext.defaultParallelism)

    val testData = streamingContext.sparkContext.textFile(eval)

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, stream)

    def historicalDataRDD(recentItems: RDD[String]) = {
      streamingContext.sparkContext.textFile(streamingSource.getProcessedFiles.mkString(","))
        .union(data)
        .union(recentItems)
        .sample(withReplacement = false, samplingRate)
        .repartition(streamingContext.sparkContext.defaultParallelism)
        .cache()
    }

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var proactiveRDD: RDD[String] = null
    var time = 0
    evaluateStream(pipeline, testData, resultPath)
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      // live statistics update
      pipeline.update(rdd)

      if (proactiveRDD == null) {
        proactiveRDD = rdd
      } else {
        proactiveRDD = proactiveRDD.union(rdd)
      }
      if (time % slack == 0) {
        // train the pipeline
        val startTime = System.currentTimeMillis()
        val nextBatch = historicalDataRDD(proactiveRDD)
        pipeline.train(nextBatch)
        val endTime = System.currentTimeMillis()

        // compute and store the training time
        val trainingTime = endTime - startTime
        storeTrainingTimes(trainingTime, resultPath)

        // evaluate the pipeline
        evaluateStream(pipeline, testData, resultPath)

        proactiveRDD = null
      }
      time += 1
    }
  }
}
