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
                       iter: Int) = {

    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

    logger.info(s"current step-size ($stepSize)")

    brzAxpy(-stepSize, asBreeze(gradient), brzWeights)

    iterCounter = iterCounter + 1

    fromBreeze(brzWeights)
  }

  override def name = "constant"
}
