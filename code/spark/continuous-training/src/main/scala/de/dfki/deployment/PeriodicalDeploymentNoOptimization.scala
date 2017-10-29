package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * Periodical Deployment With No Optimization
  *
  * @author behrouz
  */
class PeriodicalDeploymentNoOptimization(val history: String,
                                         val stream: String,
                                         val evaluationPath: String,
                                         val resultPath: String,
                                         val numIterations: Int = 500) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = (1 to 5).map(i => s"$stream/day_$i")
    var copyPipeline = pipeline

    val testData = streamingContext.sparkContext.textFile(evaluationPath)
    var trainingDays: ListBuffer[String] = new ListBuffer[String]()
    trainingDays += history
    evaluateStream(copyPipeline,testData, resultPath)

    for (day <- days) {
      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.model.setNumIterations(numIterations)
      trainingDays += day
      val data = streamingContext.sparkContext
        .textFile(trainingDays.mkString(","))
      val startTime = System.currentTimeMillis()
      copyPipeline.update(data)
      copyPipeline.train(data)
      val endTime = System.currentTimeMillis()
      evaluateStream(copyPipeline,testData, resultPath)
      storeTrainingTimes(endTime - startTime, resultPath)

    }
  }

}
