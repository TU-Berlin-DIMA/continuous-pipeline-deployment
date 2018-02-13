package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import breeze.numerics.sqrt
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithAdaDelta(var gamma: Double = 0.9) extends Updater {

  var gradientsSquared: BV[Double] = _
  var deltasSquared: BV[Double] = _

  override var iterCounter = 1

  val eps = 1E-6

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)
    val thisIterStepSize = stepSize / sqrt(iterCounter)
    val size = brzGradient.size
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](size)
      deltasSquared = BDV.zeros[Double](size)
    }
    // adjust the size of the accumulators
    // used when the size of the model is dynamically changing
    gradientsSquared = adjustSize(gradientsSquared, size)
    deltasSquared = adjustSize(deltasSquared, size)
    /**
      * break E[g^2] = gamma * E[g^2] + (1 - gamma)g^^2
      * into 2 parts for efficiency
      */
    gradientsSquared :*= gamma
    brzAxpy(1 - gamma, brzGradient :* brzGradient, gradientsSquared)

    // delta = (RMS(deltasSquared) / RMS(gradientsSquared)) * gradient
    val deltas = (sqrt(deltasSquared + eps) / sqrt(gradientsSquared + eps)) :* brzGradient

    /**
      * break deltasSquared = (deltasSquared * gamma) + ((deltas :* deltas) * (1 - gamma))
      * into 2 parts for efficiency
      */
    deltasSquared :*= gamma
    brzAxpy(1 - gamma, deltas :* deltas, deltasSquared)
    val brzWeights = asBreeze(weightsOld)
    if (regParam != 0) {
      brzWeights :*= (1.0 - thisIterStepSize * regParam)
    }

    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }
    logger.info(s"current step-size ($thisIterStepSize), regParam($regParam)")

    brzAxpy(-1.0, deltas, brzWeights)

    iterCounter = iterCounter + 1

    (fromBreeze(brzWeights), regVal)
  }

  override def name = "adadelta"
}
