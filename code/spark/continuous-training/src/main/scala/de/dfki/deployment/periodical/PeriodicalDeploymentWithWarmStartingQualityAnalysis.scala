package de.dfki.deployment.periodical

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class PeriodicalDeploymentWithWarmStartingQualityAnalysis(val history: String,
                                                          val streamBase: String,
                                                          val evaluation: String,
                                                          val resultPath: String,
                                                          val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                                          val frequency: Int = 100) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
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
    // TODO: Introduce these as pipeline level parameters
    val initialNumIterations = pipeline.model.getNumIterations
    val initialMiniBatch = 0.1
    val initialConvergenceTol = 1E-6
    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, streamBase, days = daysToProcess)

    var processedRDD: ListBuffer[RDD[String]] = new ListBuffer[RDD[String]]()
    processedRDD += data
    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    pipeline.model.setConvergenceTol(0.0)
    var time = 1
    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(pipeline, testData, resultPath, "periodical-warmstart")
    }
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)

      if (evaluation == "prequential") {
        // perform evaluation
        evaluateStream(pipeline, rdd, resultPath, "periodical-warmstart")
      }
      pipeline.updateTransformTrain(rdd)

      processedRDD += rdd

      if (time % frequency == 0) {
        // start of a new day
        pipeline.model.setMiniBatchFraction(initialMiniBatch)
        pipeline.model.setConvergenceTol(initialConvergenceTol)
        val data = streamingContext.sparkContext.union(processedRDD).repartition(streamingContext.sparkContext.defaultParallelism)
        pipeline.updateTransformTrain(data, initialNumIterations)
        pipeline.model.setMiniBatchFraction(1.0)
        pipeline.model.setNumIterations(1)
        pipeline.model.setConvergenceTol(0.0)
      }
      time += 1
    }
    processedRDD.foreach(r => r.unpersist(true))
  }

}
