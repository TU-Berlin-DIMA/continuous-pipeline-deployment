package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

/**
  * @author behrouz
  */
abstract class Deployment {
  def deploy(spark: StreamingContext, pipeline: Pipeline)

  def evaluateStream(pipeline: Pipeline,
                     evaluationData: RDD[String],
                     resultPath: String) = {

    val totalLogLoss = pipeline
      .predict(evaluationData)
      .map(pre => (LogisticLoss.logisticLoss(pre._1, pre._2), 1))
      // sum over logistic loss
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    // store the average logistic loss into file
    storeLogisticLoss(totalLogLoss._1 / totalLogLoss._2, resultPath)
  }

  val storeLogisticLoss = (logLoss: Double, resultPath: String) => {
    val file = new File(s"$resultPath/loss")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$logLoss\n")
    }
    finally fw.close()
  }

  def storeTrainingTimes(time: Long, root: String, name: String = "time") = {
    val file = new File(s"$root/$name")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$time\n")
    }
    finally fw.close()
  }
}
