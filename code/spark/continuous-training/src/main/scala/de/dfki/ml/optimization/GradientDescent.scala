package de.dfki.ml.optimization

import breeze.linalg.norm
import de.dfki.ml.LinearAlgebra
import de.dfki.ml.LinearAlgebra._
import de.dfki.ml.optimization.gradient.BatchGradient
import de.dfki.ml.optimization.updater.Updater
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD

/**
  * Technically mini batch is a property of stochastic gradient descent
  * in gradient descent the mini batch ratio is always 1
  *
  * @author bede01.
  */
class GradientDescent(var stepSize: Double,
                      var numIterations: Int,
                      var regParam: Double,
                      var convergenceTol: Double,
                      var miniBatchFraction: Double,
                      var fitIntercept: Boolean,
                      gradient: BatchGradient,
                      var updater: Updater) extends SGDOptimizer {
  @transient lazy val logger = Logger.getLogger(getClass.getName)

  var numFeatures: Int = _
  var convergedAfter: Int = 0

  def setConvergenceTol(convergenceTol: Double): this.type = {
    this.convergenceTol = convergenceTol
    this
  }

  def setRegParam(regParam: Double): this.type = {
    this.regParam = regParam
    this
  }


  def setMiniBatchFraction(miniBatchFraction: Double): this.type = {
    this.miniBatchFraction = miniBatchFraction
    this
  }


  def setNumIterations(numIterations: Int): this.type = {
    this.numIterations = numIterations
    this
  }


  def setStepSize(stepSize: Double): this.type = {
    this.stepSize = stepSize
    this
  }

  def setUpdater(updater: Updater): this.type = {
    this.updater = updater
    this
  }

  def getConvergedAfter = convergedAfter

  /**
    * :: DeveloperApi ::
    * Runs gradient descent on the given training data.
    *
    * @param data           training data
    * @param initialWeights initial weights
    * @return solution vector
    */
  def optimize(data: RDD[(Double, Vector)], initialWeights: Vector): Vector = {
    optimize(data, initialWeights, 0.0)
  }

  override def optimize(data: RDD[(Double, Vector)], initialWeights: Vector, intercept: Double): Vector = {
    val result = GradientDescent.runMiniBatchSGD(
      data = data,
      gradient = gradient,
      updater = updater,
      stepSize = stepSize,
      numIterations = numIterations,
      regParam = regParam,
      convergenceTol = convergenceTol,
      miniBatchFraction = miniBatchFraction,
      initialWeights = initialWeights,
      fitIntercept = fitIntercept,
      intercept = intercept)
    convergedAfter = result._2
    result._1
  }
}

object GradientDescent {
  @transient val logger = Logger.getLogger(getClass.getName)


  /**
    * Run stochastic gradient descent (SGD) in parallel using mini batches.
    * In each iteration, we sample a subset (fraction miniBatchFraction) of the total data
    * in order to compute a gradient estimate.
    * Sampling, and averaging the subgradients over this subset is performed using one standard
    * spark map-reduce in each iteration.
    *
    * @param data              Input data for SGD. RDD of the set of data examples, each of
    *                          the form (label, [feature values]).
    * @param gradient          Gradient object (used to compute the gradient of the loss function of
    *                          one single data example)
    * @param updater           Updater function to actually perform a gradient step in a given direction.
    * @param stepSize          initial step size for the first step
    * @param numIterations     number of iterations that SGD should be run.
    * @param regParam          regularization parameter
    * @param miniBatchFraction fraction of the input data set that should be used for
    *                          one iteration of SGD. Default value 1.0.
    * @param convergenceTol    Minibatch iteration will end before numIterations if the relative
    *                          difference between the current weight and the previous weight is less
    *                          than this value. In measuring convergence, L2 norm is calculated.
    *                          Default value 0.001. Must be between 0.0 and 1.0 inclusively.
    * @return A tuple containing two elements. The first element is a column matrix containing
    *         weights for every feature, and the second element is an array containing the
    *         stochastic loss computed for every iteration.
    */
  def runMiniBatchSGD(data: RDD[(Double, Vector)],
                      gradient: BatchGradient,
                      updater: Updater,
                      stepSize: Double,
                      numIterations: Int,
                      regParam: Double,
                      convergenceTol: Double,
                      miniBatchFraction: Double,
                      initialWeights: Vector,
                      fitIntercept: Boolean,
                      intercept: Double): (Vector, Int) = {

    // convergenceTol should be set with non minibatch settings
    if (miniBatchFraction < 1.0 && convergenceTol > 0.0) {
      logger.warn("Testing against a convergenceTol when using miniBatchFraction " +
        "< 1.0 can be unstable because of the stochasticity in sampling.")
    }

    if (numIterations * miniBatchFraction < 1.0) {
      logger.warn("Not all examples will be used if numIterations * miniBatchFraction < 1.0: " +
        s"numIterations=$numIterations and miniBatchFraction=$miniBatchFraction")
    }

    //val stochasticLossHistory = new ArrayBuffer[Double](numIterations)
    // Record previous weight and current one to calculate solution vector difference

    var previousWeights: Option[Vector] = None
    var currentWeights: Option[Vector] = None
    val reportIncrement = 10
    val lossArray = Array.ofDim[Double](reportIncrement * 2)
    var lossIndex = 0


    val numFeatures = initialWeights.size
    gradient.setNumFeatures(numFeatures)
    // Initialize weights as a column vector
    var weights = if (!fitIntercept) {
      Vectors.dense(initialWeights.toArray)
    } else {
      val initialCoefficientsWithIntercept =
        Vectors.zeros(if (fitIntercept) numFeatures + 1 else numFeatures).toArray

      initialWeights.foreachActive { case (index, value) =>
        initialCoefficientsWithIntercept(index) = value
      }
      initialCoefficientsWithIntercept(numFeatures) = intercept
      Vectors.dense(initialCoefficientsWithIntercept)
    }

    /**
      * For the first iteration, the regVal will be initialized as sum of weight squares
      * if it's L2 updater; for L1 updater, the same logic is followed.
      */
    //var regVal = updater.compute(weights, Vectors.zeros(weights.size), 0, 1, regParam)._2

    var converged = false
    var prevLoss = Double.MaxValue
    var currLoss = Double.MaxValue
    // indicates whether converged based on convergenceTol
    var i = 1
    while (!converged && i <= numIterations) {
      // this is to avoid the unnecessary sampling if sampling rate is 1.0
      val sampledData = if (miniBatchFraction == 1.0) {
        data
      }
      else {
        data.sample(withReplacement = false, miniBatchFraction)
      }

      prevLoss = currLoss
      // the gradient computation takes care of dividing the loss and gradient by the number of items in the sample
      val (lossSum, newGradients) = gradient.compute(sampledData, weights)


      weights = updater.compute(weights, LinearAlgebra.fromBreeze(newGradients), stepSize, i)

      currLoss = lossSum
      previousWeights = currentWeights
      currentWeights = Some(weights)
      // only keep the loss from the last 2 * report increments
      lossArray(lossIndex % (reportIncrement * 2)) = lossSum
      lossIndex += 1


      if (previousWeights.isDefined && currentWeights.isDefined &&
        convergenceTol > 0.0 && i % reportIncrement == 0) {
        // convergence check based on loss is cheaper. Do that first
        converged = isConverged(lossArray,
          reportIncrement,
          convergenceTol)

        if (!converged) {
          converged = isConverged(previousWeights.get,
            currentWeights.get,
            convergenceTol)
          logger.info(s"Model with regparam $regParam not converged after $i iterations")
        }
      }

      //logger.info(s"Iteration ($i/$numIterations) ,loss($currLoss)")
      i += 1
    }

    //logger.info(s"converged after ${i - 1} iterations")
    (weights, i - 1)
  }

  private def isConverged(previousWeights: Vector,
                          currentWeights: Vector,
                          convergenceTol: Double): Boolean = {
    // To compare with convergence tolerance.
    val previousBDV = asBreeze(previousWeights).toDenseVector
    val currentBDV = asBreeze(currentWeights).toDenseVector

    val solutionVecDiff: Double = norm(previousBDV - currentBDV)

    val currentBDVNorm = norm(currentBDV)

    logger.info(s"diff($solutionVecDiff) and convergenceTol(${convergenceTol * Math.max(currentBDVNorm, 1.0)})")

    solutionVecDiff < convergenceTol * Math.max(norm(currentBDV), 1.0)
  }

  // convergence check based on the changes in the average of loss value
  def isConverged(lossArray: Array[Double],
                  reportIncrement: Int,
                  convergenceTol: Double): Boolean = {
    val l1 = lossArray.slice(0, reportIncrement).sum / reportIncrement
    val l2 = lossArray.slice(reportIncrement, lossArray.length).sum / reportIncrement
    val diff = math.abs(l2 - l1)
    logger.info(s"diff($diff) and convergenceTol($convergenceTol)")
    diff < convergenceTol
  }


}
