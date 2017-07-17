package de.dfki.ml.evaluation

/**
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

  var accuracy: Double = _
  var precision: Double = _
  var recall: Double = _
  var fMeasure: Double = _
  var size: Int = _

  override def toString = {
    s"accuracy($accuracy), precision($precision), recall($recall), f-measure($fMeasure)"
  }

  // tp, fp, tn, fn
  def asCSV: String = {
    s"$tp,$fp,$tn,$fn"
  }
}

object ConfusionMatrix {
  def parseCSV(csv: String): ConfusionMatrix = {
    val parsed = csv.split(",").map(_.trim).map(_.toInt)
    new ConfusionMatrix(parsed(0), parsed(1), parsed(2), parsed(3))
  }
}
