package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.core.sampling.{RateBasedSampler, Sampler}
import de.dfki.ml.evaluation.Score
import de.dfki.ml.pipelines.Pipeline
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
abstract class Deployment(val slack: Int = 0,
                          val sampler: Sampler = new RateBasedSampler) {

  @transient lazy val logger = Logger.getLogger(getClass.getName)

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

  def storeElapsedTime(time: Long, resultPath: String, postfix: String) = {
    val file = new File(s"$resultPath/$postfix/time")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"$time\n")
    }
    finally fw.close()
  }

  val storeLogisticLoss = (score: Score, resultPath: String, postfix: String) => {
    val file = new File(s"$resultPath/$postfix/${score.scoreType()}")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    try {
      fw.write(s"${score.rawScore()}\n")
    }
    finally fw.close()
  }

  def provideHistoricalSample[T](processedRDD: ListBuffer[RDD[T]]): List[RDD[T]] = {
    sampler.sample(processedRDD)
  }
}
