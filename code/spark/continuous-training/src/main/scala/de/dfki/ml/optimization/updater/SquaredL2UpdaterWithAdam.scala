package de.dfki.ml.optimization.updater

import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector
import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import breeze.numerics.sqrt

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithAdam(beta1: Double = 0.9,
                               beta2: Double = 0.999) extends Updater {

  var gradientsSquared: BV[Double] = _
  var gradients: BV[Double] = _

  val eps = 1E-8
  var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iterDisabled: Int) = {
    val brzGradient = asBreeze(gradient)
    val thisIterStepSize = stepSize
    //stepSize / sqrt(iterCounter)
    val size = brzGradient.size
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](size)
      gradients = BDV.zeros[Double](size)
    }

    // adjust the size of the accumulators
    // used when the size of the model is dynamically changing
    gradients = adjustSize(gradients, size)
    gradientsSquared = adjustSize(gradientsSquared, size)

    // m = beta1 * m + (1 - beta1) * g
    gradients = gradients * beta1
    brzAxpy(1 - beta1, brzGradient, gradients)

    // v ^ 2 = beta2 * v & 2 + (1 - beta2) * g ^ 2
    gradientsSquared = gradientsSquared * beta2
    brzAxpy(1 - beta2, brzGradient :* brzGradient, gradientsSquared)

    // m_ = m / (1 - beta1^t)
    val bias_g = gradients / (1 - math.pow(beta1, iterCounter))

    // v_t = v / (1 - beta2^2)
    val bias_gs = gradientsSquared / (1 - math.pow(beta2, iterCounter))

    // d = (lr / (sqrt(v_) + eps)) * m_
    val deltas = (thisIterStepSize / (sqrt(bias_gs) + eps)) :* bias_g


    val brzWeights = asBreeze(weightsOld)

    logger.info(s"current step-size ($thisIterStepSize)")

    brzAxpy(-1.0, deltas, brzWeights)

    iterCounter = iterCounter + 1

    fromBreeze(brzWeights)
  }

  override def name = "adam"
}
