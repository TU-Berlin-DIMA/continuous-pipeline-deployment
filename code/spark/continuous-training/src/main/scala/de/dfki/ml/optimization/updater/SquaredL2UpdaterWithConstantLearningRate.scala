package de.dfki.ml.optimization.updater

import breeze.linalg.{Vector => BV, axpy => brzAxpy, norm => brzNorm}
import de.dfki.ml.LinearAlgebra.{asBreeze, fromBreeze}
import org.apache.spark.mllib.linalg.Vector

/**
  * @author behrouz
  */
class SquaredL2UpdaterWithConstantLearningRate extends Updater {

  override var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {

    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

    if (regParam != 0) {
      brzWeights :*= (1.0 - stepSize * regParam)
    }

    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }
    logger.info(s"current step-size ($stepSize), regParam($regParam)")

    brzAxpy(-stepSize, asBreeze(gradient), brzWeights)

    iterCounter = iterCounter + 1

    (fromBreeze(brzWeights), regVal)
  }

  override def name = "constant"
}
