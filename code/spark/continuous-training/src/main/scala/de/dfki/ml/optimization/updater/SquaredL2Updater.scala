package de.dfki.ml.optimization.updater

import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector
import breeze.linalg.{Vector => BV, axpy => brzAxpy, norm => brzNorm}

/**
  * @author behrouz
  */
class SquaredL2Updater extends Updater {

  override var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {
    // add up both updates from the gradient of the loss (= step) as well as
    // the gradient of the regularizer (= regParam * weightsOld)
    // w' = w - thisIterStepSize * (gradient + regParam * w)
    // w' = (1 - thisIterStepSize * regParam) * w - thisIterStepSize * gradient
    val thisIterStepSize = stepSize / math.sqrt(iter)
    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector
    // if regParam is 0 skip unnecessary vector computations
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

    brzAxpy(-thisIterStepSize, asBreeze(gradient), brzWeights)

    iterCounter = iterCounter + 1

    (fromBreeze(brzWeights), regVal)
  }

  override def name = "l2"
}
