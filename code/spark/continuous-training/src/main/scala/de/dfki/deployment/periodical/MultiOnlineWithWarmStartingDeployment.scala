package de.dfki.deployment.periodical

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.experiments.Params
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author behrouz
  */
class MultiOnlineWithWarmStartingDeployment(val history: String,
                                            val streamBase: String,
                                            val evaluation: String,
                                            val resultPath: String,
                                            val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                                            val frequency: Int = 100,
                                            val numPartitions: Int,
                                            val otherParams: Params,
                                            val sparkConf: SparkConf) extends Deployment {
  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    streamingContext.stop(stopSparkContext = true, stopGracefully = true)
    var copyContext = new StreamingContext(sparkConf, Seconds(1))
    pipeline.setSparkContext(copyContext.sparkContext)

    val initialNumIterations = otherParams.numIterations
    val initialMiniBatch = otherParams.miniBatch
    val initialConvergenceTol = otherParams.convergenceTol

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    pipeline.model.setConvergenceTol(0.0)


    var streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](copyContext, streamBase, days = daysToProcess)

    val ALLFILES = streamingSource.files
    while (!streamingSource.allFileProcessed()) {
      var time = 1
      // code block for deployment between two periodical trainings
      while (time <= frequency) {
        val innerStart = System.currentTimeMillis()
        val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)

        if (evaluation == "prequential") {
          // perform evaluation
          evaluateStream(pipeline, rdd, resultPath, "periodical-with-warmstarting")
        }

        pipeline.updateTransformTrain(rdd)
        time += 1
        val innerEnd = System.currentTimeMillis()
        val innerElapsed = innerEnd - innerStart
        storeElapsedTime(innerElapsed, resultPath, "periodical-with-warmstarting")
      }
      val outerStart = System.currentTimeMillis()
      logger.info(s"Initiating a new offline training")
      val lastProcessed = streamingSource.getLastIndex
      val nextBatch = history :: ALLFILES.slice(0, lastProcessed).toList
      copyContext.stop(stopSparkContext = true, stopGracefully = true)
      copyContext = new StreamingContext(sparkConf, Seconds(1))

      // copyPipeline = copyPipeline.newPipeline()
      pipeline.setSparkContext(copyContext.sparkContext)
      pipeline.model.setMiniBatchFraction(initialMiniBatch)
      pipeline.model.setConvergenceTol(initialConvergenceTol)

      val rdd = copyContext.sparkContext.textFile(path = nextBatch.mkString(",")).repartition(numPartitions)
      pipeline.updateTransformTrain(rdd, initialNumIterations)
      pipeline.model.setMiniBatchFraction(1.0)
      pipeline.model.setNumIterations(1)
      pipeline.model.setConvergenceTol(0.0)
      streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](copyContext, streamBase, days = daysToProcess)
      streamingSource.setLastIndex(lastProcessed)
      val outerEnd = System.currentTimeMillis()
      val outerElapsed = outerEnd - outerStart
      storeElapsedTime(outerElapsed, resultPath, "periodical-with-warmstarting")
    }
  }
}



