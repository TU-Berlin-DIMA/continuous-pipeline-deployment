package de.dfki.ml.evaluation

/**
  * @author behrouz
  */
class ConfusionMatrix(val tp: Int,
                      val fp: Int,
                      val tn: Int,
                      val fn: Int) {

  val accuracy = (tp + tn).toDouble / (tp + fp + tn + fn)
  val precision = tp.toDouble / (tp + fp)
  val recall = tp.toDouble / (tp + fn)
  val fMeasure = (2 * precision * recall) / (precision + recall)
  val size = tp + fp + tn + fn

  override def toString = {
    s"accuracy($accuracy), precision($precision), recall($recall), f-measure($fMeasure)"
  }

}
