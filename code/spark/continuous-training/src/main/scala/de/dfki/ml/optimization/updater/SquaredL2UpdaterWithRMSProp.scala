package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import breeze.numerics.sqrt
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithRMSProp(gamma: Double = 0.9) extends Updater {

  var gradientsSquared: BV[Double] = _

  val eps = 1E-6

  var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int) = {
    val brzGradient = asBreeze(gradient)
    // seems using any value greater than 0.001 diverges the solution
    val thisIterStepSize = stepSize
    val size = brzGradient.size
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](size)
    }
    // adjust the size of the accumulators
    // used when the size of the model is dynamically changing
    gradientsSquared = adjustSize(gradientsSquared, size)
    /**
      * break gradientsSquared = (gradientsSquared * gamma) + (brzGradient :* brzGradient) * (1 - gamma)
      * into 2 parts for efficiency
      */
    gradientsSquared :*= gamma
    brzAxpy(1 - gamma, brzGradient :* brzGradient, gradientsSquared)
    val deltas = (thisIterStepSize / sqrt(gradientsSquared + eps)) :* brzGradient

    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

    logger.info(s"current step-size ($thisIterStepSize)")

    brzAxpy(-1.0, deltas, brzWeights)

    iterCounter = iterCounter + 1

    fromBreeze(brzWeights)
  }

  override def name = "rmsprop"
}
