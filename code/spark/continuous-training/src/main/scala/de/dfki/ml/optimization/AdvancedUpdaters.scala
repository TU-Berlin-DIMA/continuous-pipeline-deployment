package de.dfki.ml.optimization


import breeze.linalg.{DenseVector => BDV, Vector => BV, axpy => brzAxpy, norm => brzNorm}
import breeze.numerics.sqrt
import de.dfki.ml.LinearAlgebra._
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.optimization.Updater

/**
  *
  * @author bede01.
  */

abstract class AdvancedUpdaters extends Updater {
  def name: String

  var iterCounter: Int

  @transient lazy val logger = Logger.getLogger(getClass.getName)
}

class NullUpdater extends AdvancedUpdaters {
  override def name = "null"

  override var iterCounter = 1

  override def compute(weightsOld: Vector, gradient: Vector, stepSize: Double, iter: Int, regParam: Double) = ???
}

class SquaredL2Updater extends AdvancedUpdaters {

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

class SquaredL2UpdaterWithStepDecay(decaySize: Int = 10) extends AdvancedUpdaters {

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

class SquaredL2UpdaterWithConstantLearningRate extends AdvancedUpdaters {

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

/**
  * Implements the learning adaptation with momentum
  *
  * @see http://sebastianruder.com/optimizing-gradient-descent/index.html#momentum
  *      for more detailed information
  * @param gamma fraction of previous update vector
  */
class SquaredL2UpdaterWithMomentum(var gamma: Double = 0.9) extends AdvancedUpdaters {

  var updateVector: BV[Double] = _

  override var iterCounter = 1

  def withUpdateVector(vector: BV[Double]): this.type = {
    updateVector = vector
    this
  }

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {

    if (updateVector == null) {
      updateVector = BDV.zeros[Double](weightsOld.size)
    }
    var brzWeights: BV[Double] = asBreeze(weightsOld).toDenseVector
    val thisIterStepSize = stepSize / math.sqrt(iterCounter)
    if (regParam != 0) {
      brzWeights :*= (1.0 - thisIterStepSize * regParam)
    }

    // break momentum update vector formula: v = v * gamma + learningRate * gradient
    // into to parts
    // part1 : v = v * gamma
    updateVector = updateVector * gamma + thisIterStepSize * asBreeze(gradient)
    // part 2: v = v + learningRate * gradient
    //brzAxpy(thisIterStepSize, asBreeze(gradient), updateVector)

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

    // w' = w - v
    brzAxpy(-1.0, updateVector, brzWeights)

    iterCounter = iterCounter + 1

    (fromBreeze(brzWeights), regVal)
  }

  override def name = "momentum"

  override def toString = {
    s"${this.getClass.getCanonicalName}, updateVector($updateVector), gamma($gamma)"
  }
}

class SquaredL2UpdaterWithAdaDelta(var gamma: Double = 0.9) extends AdvancedUpdaters {

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
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
      deltasSquared = BDV.zeros[Double](weightsOld.size)
    }
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

class SquaredL2UpdaterWithRMSProp(gamma: Double = 0.9) extends AdvancedUpdaters {

  var gradientsSquared: BV[Double] = _

  val eps = 1E-6

  var iterCounter = 1

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)
    // seems using any value greater than 0.001 diverges the solution
    val thisIterStepSize = stepSize / sqrt(iterCounter)
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
    }
    /**
      * break gradientsSquared = (gradientsSquared * gamma) + (brzGradient :* brzGradient) * (1 - gamma)
      * into 2 parts for efficiency
      */
    gradientsSquared :*= gamma
    brzAxpy(1 - gamma, brzGradient :* brzGradient, gradientsSquared)
    val deltas = (thisIterStepSize / sqrt(gradientsSquared + eps)) :* brzGradient

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

  override def name = "rmsprop"
}

class SquaredL2UpdaterWithAdam(beta1: Double = 0.9,
                               beta2: Double = 0.999) extends AdvancedUpdaters {

  var gradientsSquared: BV[Double] = _
  var gradients: BV[Double] = _

  var iterCounter = 1

  val eps = 1E-8

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iterDisabled: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)
    val thisIterStepSize = stepSize / sqrt(iterCounter)

    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
      gradients = BDV.zeros[Double](weightsOld.size)
    }

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

  override def name = "adam"
}

object AdvancedUpdaters {
  def getUpdater(updaterType: String) = {
    updaterType match {
      case "adam" => new SquaredL2UpdaterWithAdam()
      case "rmsprop" => new SquaredL2UpdaterWithRMSProp()
      case "adadelta" => new SquaredL2UpdaterWithAdaDelta()
      case "momentum" => new SquaredL2UpdaterWithMomentum()
      case "constant" => new SquaredL2UpdaterWithConstantLearningRate()
      case "l2" => new SquaredL2Updater()
      case "step-decay" => new SquaredL2UpdaterWithStepDecay()
    }
  }
}