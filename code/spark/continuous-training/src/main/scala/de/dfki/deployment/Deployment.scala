package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.ml.evaluation.{LogisticLoss, Score}
import de.dfki.ml.pipelines.Pipeline
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * @author behrouz
  */
abstract class Deployment {
  @transient private val logger = Logger.getLogger(getClass.getName)

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
      fw.write(s"${score.score()}\n")
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

  def historicalDataRDD(processedRDD: ListBuffer[RDD[String]], samplingRate: Double, slack: Int, spark: SparkContext, day: Int = -1) = {
    val now = processedRDD.size
    val history = now - slack
    val start = if (day == -1) 0 else {
      math.max(0, history - day)
    }
    if (day == 0) {
      logger.info(s"Sampling window size is $day, returning only the recent items")
      logger.info(s"${processedRDD.slice(history, now).size} items")
      spark.union(processedRDD.slice(history, now))
    }
    else if (samplingRate == 0.0) {
      spark.union(processedRDD.slice(history, now))
    } else {
      logger.info(s"Sampling window: ($start --> $history), returning a sample of historical data + all the recent items")
      val rand = new Random(System.currentTimeMillis())
      val s = if (start == 0) {
        val b = processedRDD.slice(1, history).filter(a => rand.nextDouble() < samplingRate).toList
        if (rand.nextDouble() < samplingRate) {
          processedRDD.head.sample(withReplacement = false, samplingRate) :: b
        } else {
          b
        }
      }
      else {
        processedRDD.slice(start, history).filter(a => rand.nextDouble() < samplingRate).toList
      }
      val historical = spark.union(s)
      val recent = spark.union(processedRDD.slice(history, now))
      spark.union(historical, recent)
    }
  }

}
