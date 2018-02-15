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
                       iter: Int) = {

    var multipleOfDecaySize = iter.toDouble - (iter % decaySize)
    if (multipleOfDecaySize == 0) multipleOfDecaySize = 0.5
    val thisIterStepSize = stepSize / math.sqrt(multipleOfDecaySize)

    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector

    logger.info(s"current step-size ($thisIterStepSize)")

    brzAxpy(-thisIterStepSize, asBreeze(gradient), brzWeights)

    iterCounter = iterCounter + 1

    fromBreeze(brzWeights)
  }

  override def name = "step-decay"
}
