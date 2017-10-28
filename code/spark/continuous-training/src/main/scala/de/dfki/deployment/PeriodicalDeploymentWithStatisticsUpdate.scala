package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class PeriodicalDeploymentWithStatisticsUpdate (val history: String,
                                                val stream: String,
                                                val resultPath: String,
                                                val numIterations: Int = 500) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = (1 to 5).map(i => s"$stream/day_$i")
    var copyPipeline = pipeline

    var trainingDays: ListBuffer[String] = new ListBuffer[String]()
    trainingDays += history

    for (day <- days) {
      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.model.setNumIterations(numIterations)
      trainingDays += day
      val data = streamingContext.sparkContext
        .textFile(trainingDays.mkString(","))
      copyPipeline.update(data)
      val startTime = System.currentTimeMillis()
      copyPipeline.train(data)
      val endTime = System.currentTimeMillis()

      storeTrainingTimes(endTime - startTime, resultPath)
    }
  }

}
