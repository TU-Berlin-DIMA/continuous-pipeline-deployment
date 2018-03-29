package de.dfki.deployment.baseline

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.streaming.StreamingContext

/**
  * @author behrouz
  */
class BaselineDeploymentQualityAnalysis(val streamBase: String,
                                        val evaluation: String = "prequential",
                                        val resultPath: String,
                                        val daysToProcess: Array[Int]) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val testData = streamingContext
      .sparkContext
      .textFile(evaluation)
      .setName("Evaluation Data set")
      .cache()

    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)
    var time = 1

    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(pipeline, testData, resultPath, "baseline")
    }

    while (!streamingSource.allFileProcessed()) {

      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)
      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, "baseline")
      } else {
        evaluateStream(pipeline, testData, resultPath, "baseline")
      }
      time += 1
    }
  }
}
