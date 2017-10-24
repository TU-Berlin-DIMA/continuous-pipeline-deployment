package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
class ContinuousDeployment(val history: String,
                           val stream: String,
                           val eval: String,
                           val resultPath: String,
                           val samplingRate: Double = 0.1,
                           val slack: Long = 1) extends Deployment {

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

    evaluateStream(pipeline, testData, resultPath)
    streamingSource
      .map(_._2.toString)
      // updating the statistics
      .transform(rdd => {
      pipeline.update(rdd)
      rdd
    })
      // create a window
      .window(Seconds(slack), Seconds(slack))
      // hybrid proactive training
      .transform(rdd => {
      pipeline.train(historicalDataRDD(rdd))
      rdd
    })
      // evaluate the model
      .transform(rdd => {
      evaluateStream(pipeline, testData, resultPath)
      rdd
    })
      // dummy action
      .foreachRDD(_ => {})


    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
