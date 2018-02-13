package de.dfki.ml.optimization.updater

import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithStepDecay(decaySize: Int = 10) extends Updater {

  override var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {

    var multipleOfDecaySize = iter.toDouble - (iter % decaySize)
    if (multipleOfDecaySize == 0) multipleOfDecaySize = 0.5
    val thisIterStepSize = stepSize / math.sqrt(multipleOfDecaySize)

    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

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

  override def name = "step-decay"
}
