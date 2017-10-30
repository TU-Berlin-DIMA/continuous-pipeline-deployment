package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.streaming.StreamingContext

/**
  * @author behrouz
  */
class PeriodicalDeploymentQualityAnalysis(val history: String,
                                          val streamBase: String,
                                          val evaluationPath: String,
                                          val resultPath: String,
                                          val numIterations: Int = 500,
                                          val daysToProcess: Array[Int] = Array(1, 2, 3, 4, 5)) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = Array(history) ++ daysToProcess.map(i => s"$streamBase/day_$i")
    var copyPipeline = pipeline

    val testData = streamingContext.sparkContext.textFile(evaluationPath)

    evaluateStream(copyPipeline, testData, resultPath)

    val rdds = days.map {
      input =>
        val rdd = streamingContext.sparkContext.textFile(input).cache()
        rdd.count()
        rdd
    }

    for (i <- days.indices) {
      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.model.setNumIterations(numIterations)
      // construct the data from day 0 to day i
      val data = rdds.slice(0, i + 1).reduce((a, b) => a.union(b))
      copyPipeline.updateTransformTrain(data)
      evaluateStream(copyPipeline, testData, resultPath)
    }

    rdds.foreach {
      r => r.unpersist(true)
    }
  }

}

