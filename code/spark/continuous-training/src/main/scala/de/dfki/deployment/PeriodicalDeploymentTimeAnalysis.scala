package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.streaming.StreamingContext

/**
  * Periodical Deployment With No Optimization
  *
  * @author behrouz
  */
class PeriodicalDeploymentTimeAnalysis(val history: String,
                                       val stream: String,
                                       val evaluationPath: String,
                                       val resultPath: String,
                                       val numIterations: Int = 500) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = (1 to 5).map(i => s"$stream/day_$i")
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

      // update and store update time
      val start = System.currentTimeMillis()
      copyPipeline.updateTransformTrain(data)
      val end = System.currentTimeMillis()
      val updateTime = end - start
      storeTrainingTimes(updateTime, resultPath, "total")

      evaluateStream(copyPipeline, testData, resultPath)
    }

    rdds.foreach {
      r => r.unpersist(true)
    }
  }

}
