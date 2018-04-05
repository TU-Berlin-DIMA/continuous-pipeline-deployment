package de.dfki.ml.evaluation

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
class LogisticLoss(val err: Double,
                   val count: Int) extends Score{

  val error = err / count

  override def score() = error

  override def toString = {
    s"logistic loss($error)"
  }

  override def rawScore(): String = {
    s"$err,$count"
  }

  override def scoreType() = "logistic_loss"
}

object LogisticLoss {
  val RESULT_PATH = "data/criteo-full/temp-results"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)
    val data = sc.textFile(s"$RESULT_PATH/optimizer=sgd/updater=l2-adam/iter=500/step-size=0.01-1/reg=0.0").map(parse)
    val loss = fromRDD(data)
    println(s"loss = $loss")
  }

  def fromRDD(rdd: RDD[(Double, Double)]): Score = {
    val res = rdd
      .map(i => (computeLogisticLoss(i._1, i._2), 1))
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    new LogisticLoss(res._1, res._2)
  }

  def merge(l1: LogisticLoss, l2: LogisticLoss): LogisticLoss = {
    new LogisticLoss(l1.err + l2.err, l1.count + l2.count)
  }

  private def computeLogisticLoss(label: Double, prediction: Double): Double = {
    if (prediction == label)
      0
    else {
      val eps = 10E-20
      val a = -1 * (label * math.log(prediction + eps) + (1 - label) * math.log(1 - prediction + eps))
      a
    }
  }

  private def parse(line: String): (Double, Double) = {
    val predict :: label :: other = line.split(",").map(s => s.replace("(", "").replace(")", "").toDouble).toList
    (predict, label)
  }
}
