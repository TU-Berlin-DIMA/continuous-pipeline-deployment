package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.core.sampling.{RateBasedSampler, Sampler}
import de.dfki.experiments.Params
import de.dfki.ml.evaluation.Score
import de.dfki.ml.pipelines.Pipeline
import de.dfki.ml.pipelines.criteo.CriteoPipeline
import de.dfki.ml.pipelines.nyc_taxi.NYCTaxiPipeline
import de.dfki.ml.pipelines.urlrep.URLRepPipeline
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

  def writePipeline(pipeline: Pipeline, pipelineType: String, location: String) = {
    pipelineType match {
      case "taxi" => NYCTaxiPipeline.saveToDisk(pipeline.asInstanceOf[NYCTaxiPipeline], location)
      case "url-rep" => URLRepPipeline.saveToDisk(pipeline.asInstanceOf[URLRepPipeline], location)
      case "criteo" => CriteoPipeline.saveToDisk(pipeline.asInstanceOf[CriteoPipeline], location)
    }
  }

  def decideToSavePipeline(pipeline: Pipeline, deployment: String, params: Params, time: Int) = {
    params.pipelineName match {
      case "taxi" => if (time % params.dayDuration == 0) writePipeline(pipeline, "taxi", s"${params.initialPipeline}-$deployment/$time")
      case "url-rep" => if (time % (params.dayDuration * 10) == 0) writePipeline(pipeline, "url-rep", s"${params.initialPipeline}-$deployment/$time")
      case "criteo" => if (time % params.dayDuration == 0) writePipeline(pipeline, "criteo", s"${params.initialPipeline}-$deployment/$time")
    }
  }
}
