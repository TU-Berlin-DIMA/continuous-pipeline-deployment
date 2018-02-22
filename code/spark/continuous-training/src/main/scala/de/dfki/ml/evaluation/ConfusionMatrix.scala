package de.dfki.ml.evaluation

import org.apache.spark.rdd.RDD

/**
  * Confusion Matrix for calculating classification quality metrics
  *
  * @author behrouz
  */
class ConfusionMatrix(val tp: Int,
                      val fp: Int,
                      val tn: Int,
                      val fn: Int) extends Score {

  calculate()

  def calculate() {
    accuracy = (tp + tn).toDouble / (tp + fp + tn + fn)
    precision = tp.toDouble / (tp + fp)
    recall = tp.toDouble / (tp + fn)
    fMeasure = (2 * precision * recall) / (precision + recall)
    size = tp + fp + tn + fn

  }

  private var accuracy: Double = _
  private var precision: Double = _
  private var recall: Double = _
  private var fMeasure: Double = _
  private var size: Int = _

  def getAccuracy = {
    if (accuracy.isNaN) accuracy
    else {
      calculate()
      accuracy
    }
  }

  def getPrecision = {
    if (precision.isNaN) precision
    else {
      calculate()
      precision
    }
  }

  def getRecall = {
    if (recall.isNaN) recall
    else {
      calculate()
      recall
    }
  }

  def getFMeasure = {
    if (fMeasure.isNaN) fMeasure
    else {
      calculate()
      fMeasure
    }
  }

  def getSize = {
    if (size.isNaN) size
    else {
      calculate()
      size
    }
  }

  override def score() = accuracy

  override def toString = {
    s"accuracy($accuracy), precision($precision), recall($recall), f-measure($fMeasure)"
  }

  def toFullString = {
    s"tp($tp),fp($fp),tn($tn),fn($fn),accuracy($accuracy), precision($precision), recall($recall), f-measure($fMeasure)"
  }

  def resultAsCSV = {
    s"$tp,$fp,$tn,$fn,$accuracy,$precision,$recall,$fMeasure"
  }


  // tp, fp, tn, fn
  def rawScore(): String = {
    s"$tp,$fp,$tn,$fn"
  }

  override def scoreType() = "confusion_matrix"
}

object ConfusionMatrix {
  def fromCSVLine(csv: String): ConfusionMatrix = {
    val parsed = csv.split(",").map(_.trim).map(_.toInt)
    new ConfusionMatrix(parsed(0), parsed(1), parsed(2), parsed(3))
  }

  def fromRDD(rdd: RDD[(Double, Double)]): ConfusionMatrix = {
    rdd
      .map {
        v =>
          var tp, fp, tn, fn = 0
          if (v._1 == v._2 & v._1 == 1.0) tp = 1
          else if (v._1 == v._2 & v._1 == 0.0) tn = 1
          else if (v._1 != v._2 & v._1 == 1.0) fp = 1
          else fn = 1
          new ConfusionMatrix(tp, fp, tn, fn)
      }
      .reduce((c1, c2) => ConfusionMatrix.merge(c1, c2))
  }

  def merge(c1: ConfusionMatrix, c2: ConfusionMatrix): ConfusionMatrix = {
    new ConfusionMatrix(c1.tp + c2.tp, c1.fp + c2.fp, c1.tn + c2.tn, c1.fn + c2.fn)
  }
}
