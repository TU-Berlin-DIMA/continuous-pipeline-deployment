package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class PeriodicalDeploymentWithStatisticsUpdate (val history: String,
                                                val days: Array[String],
                                                val eval: String,
                                                val resultPath: String) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    var copyPipeline = pipeline
    val testData = streamingContext.sparkContext.textFile(eval)

    // initial evaluation
    evaluateStream(copyPipeline, testData, resultPath)
    var trainingDays: ListBuffer[String] = new ListBuffer[String]()
    trainingDays += history

    for (day <- days) {
      copyPipeline = copyPipeline.newPipeline()
      trainingDays += day
      val data = streamingContext.sparkContext
        .textFile(trainingDays.mkString(","))
        .repartition(streamingContext.sparkContext.defaultParallelism)
      copyPipeline.update(data)
      val startTime = System.currentTimeMillis()
      copyPipeline.train(data)
      val endTime = System.currentTimeMillis()

      storeTrainingTimes(endTime - startTime, resultPath)

      evaluateStream(copyPipeline, testData, resultPath)
    }
  }

}
