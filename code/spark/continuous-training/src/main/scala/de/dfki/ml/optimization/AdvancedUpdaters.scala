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

  @transient lazy val logger = Logger.getLogger(getClass.getName)
}

class NullUpdater extends AdvancedUpdaters {
  override def name = "null"

  override def compute(weightsOld: Vector, gradient: Vector, stepSize: Double, iter: Int, regParam: Double) = ???
}

class SquaredL2Updater extends AdvancedUpdaters {
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

  override def name = "l2"
}

class SquaredL2UpdaterWithStepDecay(decaySize: Int) extends AdvancedUpdaters {
  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double): (Vector, Double) = {
    // add up both updates from the gradient of the loss (= step) as well as
    // the gradient of the regularizer (= regParam * weightsOld)
    // w' = w - thisIterStepSize * (gradient + regParam * w)
    // w' = (1 - thisIterStepSize * regParam) * w - thisIterStepSize * gradient
    var multipleOfDecaySize = iter.toDouble - (iter % decaySize)
    if (multipleOfDecaySize == 0) multipleOfDecaySize = 0.5
    val thisIterStepSize = stepSize / math.sqrt(multipleOfDecaySize)
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

  override def name = "l2-step-decay"
}

class SquaredL2UpdaterWithConstantLearningRate extends AdvancedUpdaters {
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

  override def name = "l2-constant"
}

/**
  * Implements the learning adaptation with momentum
  *
  * @see http://sebastianruder.com/optimizing-gradient-descent/index.html#momentum
  *      for more detailed information
  * @param gamma fraction of previous update vector
  */
class SquaredL2UpdaterWithMomentum(var gamma: Double) extends AdvancedUpdaters {
  var updateVector: BV[Double] = _

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
    val thisIterStepSize = stepSize / math.sqrt(iter)
    if (regParam != 0) {
      brzWeights :*= (1.0 - thisIterStepSize * regParam)
    }

    // momentum update vector formula: v = v * gamma + learningRate * gradient
    updateVector = updateVector * gamma + asBreeze(gradient) * thisIterStepSize

    // w' = w - v
    brzWeights = brzWeights - updateVector
    //brzAxpy(-1.0, updateVector, brzWeights)
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

  override def name = "l2-momentum"

  override def toString = {
    s"${this.getClass.getCanonicalName}, updateVector($updateVector), gamma($gamma)"
  }
}

class SquaredL2UpdaterWithAdaDelta(var gamma: Double) extends AdvancedUpdaters {

  var gradientsSquared: BV[Double] = _
  var deltasSquared: BV[Double] = _

  val eps = 1E-6

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
      deltasSquared = BDV.zeros[Double](weightsOld.size)
    }
    // E[g^2] = gamma * E[g^2] + (1 - gamma)g^2
    gradientsSquared = (gradientsSquared * gamma) + (brzGradient :* brzGradient) * (1 - gamma)

    // delta = (RMS(deltasSquared) / RMS(gradientsSquared)) * gradient
    val deltas = (sqrt(deltasSquared + eps) / sqrt(gradientsSquared + eps)) :* brzGradient
    deltasSquared = (deltasSquared * gamma) + ((deltas :* deltas) * (1 - gamma))
    var brzWeights = asBreeze(weightsOld).toDenseVector

    brzWeights = brzWeights - deltas
    (fromBreeze(brzWeights), 0.0)
  }

  override def name = "l2-adadelta"
}

class SquaredL2UpdaterWithRMSProp(gamma: Double) extends AdvancedUpdaters {

  var gradientsSquared: BV[Double] = _

  val eps = 1E-6
  /**
    * recommend constant size value for RMSProp
    * Step size values greater than these are ignored and replaced
    * by 0.001
    */
  val stepSize = 0.001

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)
    // seems using any value greater than 0.001 diverges the solution
    val thisIterStepSize = if (stepSize > this.stepSize) this.stepSize else stepSize
    // initialize the update vectors
    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
    }

    // E[g^2] = gamma * E[g^2] + (1 - gamma)g^2
    gradientsSquared = (gradientsSquared * gamma) + (brzGradient :* brzGradient) * (1 - gamma)

    val deltas = (thisIterStepSize / sqrt(gradientsSquared + eps)) :* brzGradient

    var brzWeights = asBreeze(weightsOld).toDenseVector
    brzWeights = brzWeights - deltas

    (fromBreeze(brzWeights), 0.0)
  }

  override def name = "l2-rmsprop"
}

class SquaredL2UpdaterWithAdam(beta1: Double,
                               beta2: Double) extends AdvancedUpdaters {

  var gradientsSquared: BV[Double] = _
  var gradients: BV[Double] = _

  val eps = 1E-8

  override def compute(weightsOld: Vector,
                       gradient: Vector,
                       stepSize: Double,
                       iter: Int,
                       regParam: Double) = {
    val brzGradient = asBreeze(gradient)

    if (gradientsSquared == null) {
      gradientsSquared = BDV.zeros[Double](weightsOld.size)
      gradients = BDV.zeros[Double](weightsOld.size)
    }

    // m = beta1 * m + (1 - beta1) * g
    gradients = gradients * beta1 + brzGradient * (1 - beta1)
    // v ^ 2 = beta2 * v & 2 + (1 - beta2) * g ^ 2
    gradientsSquared = (gradientsSquared * beta2) + (brzGradient :* brzGradient) * (1 - beta2)

    // m_ = m / (1 - beta1^t)
    val bias_g = gradients / (1 - math.pow(beta1, iter))

    // v_t = v / (1 - beta2^2)
    val bias_gs = gradientsSquared / (1 - math.pow(beta2, iter))

    // d = (lr / (sqrt(v_) + eps)) * m_
    val deltas = (stepSize / (sqrt(bias_gs) + eps)) :* bias_g

    var brzWeights = asBreeze(weightsOld).toDenseVector
    brzWeights = brzWeights - deltas

    (fromBreeze(brzWeights), 0.0)
  }

  override def name = "l2-adam"
}

