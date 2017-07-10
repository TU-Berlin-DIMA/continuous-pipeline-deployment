package de.dfki.ml.optimization


import breeze.linalg.{Vector => BV, axpy => brzAxpy, norm => brzNorm}
import de.dfki.ml.LinearAlgebra._
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.optimization.Updater

/**
  * Copy of @see org.apache.spark.mllib.optimization.SquaredL2Updater
  *
  * @author bede01.
  */

class AdvancedUpdaters {

}

class SquaredL2Updater extends Updater {
  val logger = Logger.getLogger(getClass.getName)

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
    if (regParam != 0) {
      brzWeights :*= (1.0 - stepSize * regParam)
    }
    brzAxpy(-thisIterStepSize, asBreeze(gradient), brzWeights)
    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }
    logger.info(s"current step-size ($thisIterStepSize), regParam($regParam)")

    (fromBreeze(brzWeights), regVal)
  }
}

class SquaredL2UpdaterWithStepDecay extends Updater {
  val logger = Logger.getLogger(getClass.getName)

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {
    // add up both updates from the gradient of the loss (= step) as well as
    // the gradient of the regularizer (= regParam * weightsOld)
    // w' = w - thisIterStepSize * (gradient + regParam * w)
    // w' = (1 - thisIterStepSize * regParam) * w - thisIterStepSize * gradient
    var multipleOf10Iter = iter.toDouble - (iter % 10)
    if (multipleOf10Iter == 0) multipleOf10Iter = 0.5
    val thisIterStepSize = stepSize / math.sqrt(multipleOf10Iter)
    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector
    if (regParam != 0) {
      brzWeights :*= (1.0 - stepSize * regParam)
    }
    brzAxpy(-thisIterStepSize, asBreeze(gradient), brzWeights)
    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }

    logger.info(s"current step-size ($thisIterStepSize), regParam($regParam)")

    (fromBreeze(brzWeights), regVal)

  }

}

class SquaredL2UpdaterWithConstantLearningRate extends Updater {
  val logger = Logger.getLogger(getClass.getName)

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {
    // add up both updates from the gradient of the loss (= step) as well as
    // the gradient of the regularizer (= regParam * weightsOld)
    // w' = w - thisIterStepSize * (gradient + regParam * w)
    // w' = (1 - thisIterStepSize * regParam) * w - thisIterStepSize * gradient
    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector
    // if regParam is 0 skip unnecessary vector computations
    if (regParam != 0) {
      brzWeights :*= (1.0 - stepSize * regParam)
    }
    brzAxpy(-stepSize, asBreeze(gradient), brzWeights)
    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }

    logger.info(s"current step-size ($stepSize), regParam($regParam)")

    (fromBreeze(brzWeights), regVal)
  }

}

/**
  * Implements the learning adaptation with momentum
  * @see  http://sebastianruder.com/optimizing-gradient-descent/index.html#momentum
  *       for more detailed information
  *
  * @param gamma fraction of previous update vector
  */
class SquaredL2UpdaterWithMomentum(gamma: Double) extends Updater {
  val logger = Logger.getLogger(getClass.getName)
  var updateVector: BV[Double] = _


  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    // add up both updates from the gradient of the loss (= step) as well as
    // the gradient of the regularizer (= regParam * weightsOld)
    // w' = w - thisIterStepSize * (gradient + regParam * w)
    // w' = (1 - thisIterStepSize * regParam) * w - thisIterStepSize * gradient
    val thisIterStepSize = stepSize // / math.sqrt(iter)
    val brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector
    if (regParam != 0) {
      brzWeights :*= (1.0 - stepSize * regParam)
    }
    val delta = asBreeze(gradient) * thisIterStepSize
    if (updateVector == null) {
      logger.info("updateVector is null, initializing it with delta value")
      updateVector = delta
    } else {
      updateVector = updateVector * gamma + delta
    }
    brzAxpy(-1.0, updateVector, brzWeights)
    val regVal = if (regParam == 0) {
      regParam
    }
    else {
      val norm = brzNorm(brzWeights, 2.0)
      0.5 * regParam * norm * norm
    }
    logger.info(s"current step-size ($thisIterStepSize), regParam($regParam)")

    (fromBreeze(brzWeights), regVal)


  }
}

