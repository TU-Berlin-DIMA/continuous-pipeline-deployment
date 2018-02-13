package de.dfki.ml.pipelines.criteo

/**
  * @author behrouz
  */
object GLOBAL_VARIABLES {

  case class RawType(label: Double, numerical: Array[Double], categorical: Array[String])

  val NUM_LABELS = 1
  val NUM_INTEGER_FEATURES = 13
  val NUM_CATEGORICAL_FEATURES = 26
  val NUM_FEATURES = NUM_LABELS + NUM_INTEGER_FEATURES + NUM_CATEGORICAL_FEATURES
}
