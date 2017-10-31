package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.log4j.Logger
import org.apache.spark.streaming.StreamingContext

/**
  * Periodical Deployment With No Optimization
  *
  * @author behrouz
  */
class PeriodicalDeploymentTimeAnalysis(val history: String,
                                       val streamBase: String,
                                       val evaluationPath: String,
                                       val resultPath: String,
                                       val numIterations: Int = 500,
                                       val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5)) extends Deployment {

  @transient private val logger = Logger.getLogger(getClass.getName)

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = Array(history) ++ daysToProcess.map(i => s"$streamBase/day_$i")
    var copyPipeline = pipeline

    val rdds = days.map {
      input =>
        val rdd = streamingContext.sparkContext.textFile(input).cache()
        rdd.count()
        rdd
    }

    // train from day 1 onward
    for (i <- 1 until days.length) {

      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.model.setNumIterations(numIterations)
      // construct the data from day 0 to day i
      val data = streamingContext.sparkContext.union(rdds.slice(0, i + 1))
      logger.info(s"scheduling a training for days [${rdds.slice(0, i + 1).length}]")
      // update and store update time
      val start = System.currentTimeMillis()
      copyPipeline.updateTransformTrain(data)
      val end = System.currentTimeMillis()
      val updateTime = end - start
      storeTrainingTimes(updateTime, resultPath, "total")
    }

    rdds.foreach {
      r => r.unpersist(true)
    }
  }

}
