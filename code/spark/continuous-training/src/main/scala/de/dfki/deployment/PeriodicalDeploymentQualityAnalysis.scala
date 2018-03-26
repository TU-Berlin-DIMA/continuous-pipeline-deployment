package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
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
class PeriodicalDeploymentQualityAnalysis(val history: String,
                                          val streamBase: String,
                                          val evaluation: String,
                                          val resultPath: String,
                                          val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                          val dayDuration: Int = 100) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .setName("Historical data")
      .cache()
    data.count()
    var copyPipeline = pipeline
    val testData = streamingContext
      .sparkContext
      .textFile(evaluation)
      .setName("Evaluation Data set")
      .cache()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data

    var time = 1
    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(copyPipeline, testData, resultPath, "periodical")
    }
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      rdd.setName(s"Stream $time")
      rdd.persist(StorageLevel.MEMORY_AND_DISK)

      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(copyPipeline, rdd, resultPath, "periodical")
      }
      processedRDD += rdd
      if (time % dayDuration == 0) {
        // start of a new day
        copyPipeline = copyPipeline.newPipeline()
        val data = streamingContext.sparkContext.union(processedRDD)
        copyPipeline.updateTransformTrain(data)
      }
      time += 1
    }
    processedRDD.foreach(r => r.unpersist(true))
  }

}

