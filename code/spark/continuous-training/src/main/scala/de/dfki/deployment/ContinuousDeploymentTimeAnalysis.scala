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
  * no optimization
  *
  * @author behrouz
  */
class ContinuousDeploymentTimeAnalysis(history: String,
                                       streamBase: String,
                                       evaluationPath: String,
                                       resultPath: String,
                                       daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                       slack: Int = 10,
                                       sampler: Sampler) extends Deployment(slack, sampler) {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .setName("Historical data")
      .cache()
    data.count()


    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)

    var time = 1
    while (!streamingSource.allFileProcessed()) {

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream $time")
      rdd.cache()
      rdd.count()

      processedRDD += rdd

      // update and store update time
      var start = System.currentTimeMillis()
      pipeline.update(rdd)
      var end = System.currentTimeMillis()
      val updateTime = end - start

      storeTrainingTimes(updateTime, s"$resultPath/${sampler.name}", "update")

      if (time % slack == 0) {

        // transform and store transform time
        start = System.currentTimeMillis()

        val nextBatch = provideHistoricalSample(processedRDD, streamingContext.sparkContext)
        val transformed = pipeline.transform(nextBatch)
        transformed.cache()
        transformed.count()
        end = System.currentTimeMillis()
        val transformTime = end - start
        storeTrainingTimes(transformTime, s"$resultPath/${sampler.name}", "transform")

        // train and store train time
        start = System.currentTimeMillis()
        pipeline.train(transformed)
        end = System.currentTimeMillis()
        val trainTime = end - start
        storeTrainingTimes(trainTime, s"$resultPath/${sampler.name}", "train")

        transformed.unpersist(true)
      }
      time += 1
    }
    processedRDD.foreach {
      r => r.unpersist(true)
    }
  }

}
