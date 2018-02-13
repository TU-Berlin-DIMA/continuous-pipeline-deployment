package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV}
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.Vector

/**
  *
  * @author behrouz
  */

abstract class Updater extends Serializable{
  def name: String

  var iterCounter: Int

  def compute(weightsOld: Vector, gradient: Vector, stepSize: Double, iter: Int, regParam: Double): (Vector, Double)

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def adjustSize(gradient: BV[Double], newSize: Int): BV[Double] = {
    val curSize = gradient.size
    if (newSize > curSize) {
      BDV.vertcat(gradient.toDenseVector, BDV.zeros[Double](newSize - curSize))
    } else {
      gradient
    }
  }
}

object Updater {
  def getUpdater(updaterType: String) = {
    updaterType match {
      case "adam" => new SquaredL2UpdaterWithAdam()
      case "rmsprop" => new SquaredL2UpdaterWithRMSProp()
      case "adadelta" => new SquaredL2UpdaterWithAdaDelta()
      case "momentum" => new SquaredL2UpdaterWithMomentum()
      case "constant" => new SquaredL2UpdaterWithConstantLearningRate()
      case "l2" => new SquaredL2Updater()
      case "step-decay" => new SquaredL2UpdaterWithStepDecay()
    }
  }
}