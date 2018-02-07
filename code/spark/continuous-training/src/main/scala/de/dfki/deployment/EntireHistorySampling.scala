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
class EntireHistorySampling(val history: String,
                            val stream: String,
                            val evaluation: String = "prequential",
                            val resultPath: String,
                            val samplingRate: Double = 0.1,
                            val slack: Int = 10) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .cache()
    data.count()

    val testData = streamingContext
      .sparkContext
      .textFile(evaluation)
      .setName("Evaluation Data set")
      .cache()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, stream)

    val processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var time = 1

    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(pipeline, testData, resultPath)
    }


    while (!streamingSource.allFileProcessed()) {

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      //pipeline.update(rdd)
      rdd.cache()
      rdd.count()

      processedRDD += rdd

      // update and store update time
      var start = System.currentTimeMillis()
      pipeline.update(rdd)
      var end = System.currentTimeMillis()
      val updateTime = end - start

      storeTrainingTimes(updateTime, resultPath, "entire")

      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStreamPrequential(pipeline, rdd, resultPath)
      }

      if (time % slack == 0) {

        // transform and store transform time
        start = System.currentTimeMillis()
        val nextBatch = historicalDataRDD(processedRDD, slack)
        val transformed = pipeline.transform(nextBatch)
        end = System.currentTimeMillis()
        val transformTime = end - start
        storeTrainingTimes(transformTime, resultPath, "transform")

        transformed.cache()
        transformed.count()
        // train and store train time
        start = System.currentTimeMillis()
        pipeline.train(transformed)
        end = System.currentTimeMillis()
        storeTrainingTimes(transformTime, resultPath, "train")

        transformed.unpersist(true)
        if (evaluation != "prequential") {
          // if evaluation method is not prequential, only perform evaluation after a training step
          evaluateStream(pipeline, testData, resultPath)
        }
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
