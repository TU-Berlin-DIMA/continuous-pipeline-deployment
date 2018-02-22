package de.dfki.ml.evaluation

/**
  * @author behrouz
  */
trait Score extends Serializable {

  def toString: String

  def rawScore(): String

  def score(): Double

  def scoreType(): String

}
