package de.dfki.ml.evaluation

import org.apache.spark.rdd.RDD

import scala.math._

/**
  * Root Mean Squared Logarithmic Error calculated based on:
  * https://www.kaggle.com/wiki/RootMeanSquaredLogarithmicError
  *
  * @author behrouz
  */
class RMSLE(val sumOfSquaredLogs: Double, val count: Double) extends Score {

  override def rawScore() = s"$sumOfSquaredLogs,$count"

  override def score() = sqrt(sumOfSquaredLogs / count)

  override def scoreType() = "rmsle"
}

object RMSLE {
  def fromRDD(rdd: RDD[(Double, Double)]): Score = {
    val res = rdd
      .map(i => (computeLogisticLoss(i._1, i._2), 1))
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    new RMSLE(res._1, res._2)
  }

  def merge(l1: RMSLE, l2: RMSLE): RMSLE = {
    new RMSLE(l1.sumOfSquaredLogs + l2.sumOfSquaredLogs, l1.count + l2.count)
  }

  private def computeLogisticLoss(label: Double, prediction: Double): Double = {
    pow(log(prediction + 1) - log(label), 2)
  }
}
