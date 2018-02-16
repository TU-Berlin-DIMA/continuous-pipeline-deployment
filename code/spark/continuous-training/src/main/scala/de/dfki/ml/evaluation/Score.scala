package de.dfki.ml.evaluation

/**
  * @author behrouz
  */
trait Score extends Serializable {

  def toString(): String

  def asCSV(): String

  def score(): Double

}
