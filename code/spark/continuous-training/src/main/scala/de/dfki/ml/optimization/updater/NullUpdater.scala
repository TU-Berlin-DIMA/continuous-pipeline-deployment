package de.dfki.ml.optimization.updater

import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class NullUpdater extends Updater {
  override def name = "null"

  override var iterCounter = 1

  override def compute(weightsOld: Vector, gradient: Vector, stepSize: Double, iter: Int) = ???
}
