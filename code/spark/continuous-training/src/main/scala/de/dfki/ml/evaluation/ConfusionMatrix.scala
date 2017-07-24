package de.dfki.ml.evaluation

/**
  * Confusion Matrix for calculating classification quality metrics
  * @author behrouz
  */
class ConfusionMatrix(val tp: Int,
                      val fp: Int,
                      val tn: Int,
                      val fn: Int) extends Serializable {

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

  override def toString = {
    s"accuracy($accuracy), precision($precision), recall($recall), f-measure($fMeasure)"
  }


  def resultAsCSV = {
    s"$tp,$fp,$tn,$fn,$accuracy,$precision,$recall,$fMeasure"
  }


  // tp, fp, tn, fn
  def asCSV: String = {
    s"$tp,$fp,$tn,$fn"
  }
}

object ConfusionMatrix {
  def fromCSVLine(csv: String): ConfusionMatrix = {
    val parsed = csv.split(",").map(_.trim).map(_.toInt)
    new ConfusionMatrix(parsed(0), parsed(1), parsed(2), parsed(3))
  }

  def merge(c1: ConfusionMatrix, c2: ConfusionMatrix): ConfusionMatrix = {
    new ConfusionMatrix(c1.tp + c2.tp, c1.fp + c2.fp, c1.tn + c2.tn, c1.fn + c2.fn)
  }
}
