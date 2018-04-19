package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import breeze.numerics.sqrt
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithAdaDelta(var gamma: Double = 0.95) extends Updater {

  var gradientsSquared: BV[Double] = _
  var deltasSquared: BV[Double] = _

  override var iterCounter = 1

  val eps = 1E-6

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int) = {
    val brzGradient = asBreeze(gradient)
    val thisIterStepSize = stepSize
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
    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

    brzAxpy(-1.0, deltas, brzWeights)

    iterCounter = iterCounter + 1
    if (iterCounter % 100 == 0) {
      logger.info(s"learning rate tuning using a default rate of :$thisIterStepSize")
    }

    fromBreeze(brzWeights)
  }

  override def name = "adadelta"
}
