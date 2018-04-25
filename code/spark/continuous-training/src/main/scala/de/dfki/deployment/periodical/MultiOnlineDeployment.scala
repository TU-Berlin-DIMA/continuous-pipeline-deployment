package de.dfki.deployment.periodical

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.Deployment
import de.dfki.ml.pipelines.Pipeline
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * hacky solution for more optimized periodical deployment
  *
  * @author behrouz
  */
class MultiOnlineDeployment(val history: String,
                            val streamBase: String,
                            val evaluation: String,
                            val resultPath: String,
                            val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                            val frequency: Int = 100,
                            val sparkConf: SparkConf) extends Deployment {
  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // create rdd of the initial data that the pipeline was trained with
    var copyPipeline = pipeline
    streamingContext.stop(stopSparkContext = true, stopGracefully = true)

//    val conf = new SparkConf().setAppName("Optimization Time Experiment")
//    val masterURL = conf.get("spark.master", "local[*]")
//    conf.setMaster(masterURL)
    var copyContext = new StreamingContext(sparkConf, Seconds(1))
    copyPipeline.setSparkContext(copyContext.sparkContext)

    val testData = copyContext
      .sparkContext
      .textFile(evaluation)
      .setName("Evaluation Data set")
      .cache()
    // TODO: Introduce these as pipeline level parameters
    val initialNumIterations = copyPipeline.model.getNumIterations
    val initialMiniBatch = 0.1
    val initialConvergenceTol = 1E-6

    copyPipeline.model.setMiniBatchFraction(1.0)
    copyPipeline.model.setNumIterations(1)
    copyPipeline.model.setConvergenceTol(0.0)


    var streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](copyContext, streamBase, days = daysToProcess)

    val ALLFILES = streamingSource.files
    if (evaluation != "prequential") {
      // initial evaluation of the pipeline right after deployment for non prequential based method
      evaluateStream(copyPipeline, testData, resultPath, "periodical-no-warmstarting")
    }
    while (!streamingSource.allFileProcessed()) {
      var time = 1
      // code block for deployment between two periodical trainings
      while (time <= frequency) {
        val innerStart = System.currentTimeMillis()
        val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)

        if (evaluation == "prequential") {
          // perform evaluation
          evaluateStream(copyPipeline, rdd, resultPath, "periodical-no-warmstarting")
        } else {
          evaluateStream(copyPipeline, testData, resultPath, "periodical-no-warmstarting")
        }
        copyPipeline.updateTransformTrain(rdd)
        time += 1
        val innerEnd = System.currentTimeMillis()
        val innerElapsed = innerEnd - innerStart
        storeElapsedTime(innerElapsed, resultPath, "periodical-no-warmstarting")
      }
      val outerStart = System.currentTimeMillis()
      logger.info(s"Initiating a new offline training")
      val lastProcessed = streamingSource.getLastIndex
      val nextBatch = history :: ALLFILES.slice(0, lastProcessed).toList
      copyContext.stop(stopSparkContext = true, stopGracefully = true)
      copyContext = new StreamingContext(sparkConf, Seconds(1))

      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.setSparkContext(copyContext.sparkContext)
      copyPipeline.model.setMiniBatchFraction(initialMiniBatch)
      copyPipeline.model.setConvergenceTol(initialConvergenceTol)
      val rdd = copyContext.sparkContext.textFile(path = nextBatch.mkString(","))
      copyPipeline.updateTransformTrain(rdd, initialNumIterations)
      copyPipeline.model.setMiniBatchFraction(1.0)
      copyPipeline.model.setNumIterations(1)
      copyPipeline.model.setConvergenceTol(0.0)
      streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](copyContext, streamBase, days = daysToProcess)
      streamingSource.setLastIndex(lastProcessed)
      val outerEnd = System.currentTimeMillis()
      val outerElapsed = outerEnd - outerStart
      storeElapsedTime(outerElapsed, resultPath, "periodical-no-warmstarting")
    }
  }
}



