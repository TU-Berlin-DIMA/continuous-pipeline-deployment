package de.dfki.deployment.rolling

import de.dfki.core.sampling.RollingWindowProvider
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
class RollingRetraining(val history: String,
                        val streamBase: String,
                        val evaluation: String,
                        val resultPath: String,
                        val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5),
                        val frequency: Int = 100,
                        val rollingWindowSize: Int = 10,
                        val numPartitions: Int,
                        val otherParams: Params,
                        val sparkConf: SparkConf) extends Deployment {

  val HISTORICAL_DATA_INDEX = 0

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
    var time = otherParams.initTime
    logger.info(s"Setting the time to $time")
    streamingSource.setLastIndex(time - 1)
    var mustRetrain = true
    while (!streamingSource.allFileProcessed()) {

      // code block for deployment between two periodical trainings
      while (time % frequency != 0 || !mustRetrain) {
        mustRetrain = true
        val innerStart = System.currentTimeMillis()
        val rdd = streamingSource.generateNextRDD().get.map(_._2.toString)

        if (evaluation == "prequential") {
          // perform evaluation
          evaluateStream(pipeline, rdd, resultPath, "rolling-with-warmstarting")
        }

        pipeline.updateTransformTrain(rdd)
        time += 1
        val innerEnd = System.currentTimeMillis()
        val innerElapsed = innerEnd - innerStart
        storeElapsedTime(innerElapsed, resultPath, "rolling-with-warmstarting")
      }
      val outerStart = System.currentTimeMillis()
      logger.info(s"Initiating a new offline training")
      val lastProcessed = streamingSource.getLastIndex
      val rollingWindowProvider = new RollingWindowProvider(rollingWindowSize)
      val nextIndices = rollingWindowProvider.sampleIndices((0 to lastProcessed).toList)

      val nextBatch = if (nextIndices contains HISTORICAL_DATA_INDEX){
        // index 0 is the history, if it exist in the roll we should drop the last item so we have
        // the correct number of micro batches
        // TODO: history is not always one index and therefore it is much larger than one micro-batch
        // this probably does not have a large impact but if we have time we should fix it so that the data
        // in the history is divided in to micro-batches that match the ones in the streaming part
        history :: nextIndices.dropRight(1).map(i => ALLFILES(i))
      } else {
        nextIndices.map(i => ALLFILES(i))
      }

        copyContext.stop(stopSparkContext = true, stopGracefully = true)
      copyContext = new StreamingContext(sparkConf, Seconds(1))

      // copyPipeline = copyPipeline.newPipeline()
      pipeline.setSparkContext(copyContext.sparkContext)
      pipeline.model.setMiniBatchFraction(initialMiniBatch)
      pipeline.model.setConvergenceTol(initialConvergenceTol)

      val rdd = copyContext.sparkContext.textFile(path = nextBatch.mkString(",")).repartition(numPartitions)
      pipeline.updateTransformTrain(rdd, initialNumIterations)
      mustRetrain = false
      writePipeline(pipeline, otherParams.pipelineName, s"${otherParams.initialPipeline}-rolling($rollingWindowSize)/$time")
      pipeline.model.setMiniBatchFraction(1.0)
      pipeline.model.setNumIterations(1)
      pipeline.model.setConvergenceTol(0.0)
      streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](copyContext, streamBase, days = daysToProcess)
      streamingSource.setLastIndex(lastProcessed)
      val outerEnd = System.currentTimeMillis()
      val outerElapsed = outerEnd - outerStart
      storeElapsedTime(outerElapsed, resultPath, "rolling-with-warmstarting")
    }
  }
}
