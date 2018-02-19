package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.core.sampling.{Sampler, SimpleRandomSampler}
import de.dfki.ml.evaluation.Score
import de.dfki.ml.pipelines.Pipeline
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
abstract class Deployment(val slack: Int = 0,
                          val sampler: Sampler = new SimpleRandomSampler) {
  def deploy(spark: StreamingContext, pipeline: Pipeline)

  def evaluateStream(pipeline: Pipeline,
                     evaluationData: RDD[String],
                     resultPath: String,
                     postfix: String = "") = {

    val score = pipeline
      .score(evaluationData)
    // store the average logistic loss into file
    storeLogisticLoss(score, resultPath, postfix)
  }

  val storeLogisticLoss = (score: Score, resultPath: String, postfix: String) => {
    val file = new File(s"$resultPath/loss_$postfix")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"${score.asCSV()}\n")
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

  def provideHistoricalSample(processedRDD: ListBuffer[RDD[String]], spark: SparkContext): RDD[String] = {
    val now = processedRDD.size
    val history = now - slack
    val recent = spark.union(processedRDD.slice(history, now))
    val historySample = sampler.sample(processedRDD.slice(0, history), spark)
    spark.union(historySample, recent)
  }

}
