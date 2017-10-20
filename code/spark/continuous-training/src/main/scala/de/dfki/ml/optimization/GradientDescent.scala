package de.dfki.ml.optimization

import breeze.linalg.norm
import de.dfki.ml.LinearAlgebra
import de.dfki.ml.LinearAlgebra._
import org.apache.log4j.Logger
import org.apache.spark.mllib.linalg.{DenseVector, Vector, Vectors}
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.stat.MultivariateOnlineSummarizer
import org.apache.spark.rdd.RDD

/**
  * Technically mini batch is a property of stochastic gradient descent
  * in gradient descent the mini batch ratio is always 1
  *
  * @author bede01.
  */
class GradientDescent(var numIterations: Int,
                      var stepSize: Double,
                      var regParam: Double,
                      var miniBatchFraction: Double,
                      var convergenceTol: Double,
                      var standardize: Boolean,
                      var fitIntercept: Boolean,
                      gradient: BatchGradient,
                      var updater: Updater) extends SGDOptimizer {
  @transient lazy val logger = Logger.getLogger(getClass.getName)


  var featuresMean: Array[Double] = _
  var featuresStd: Array[Double] = _
  var numFeatures: Int = _
  var summarizer: MultivariateOnlineSummarizer = _

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

  def this() = this(100, 1.0, 0.0, 1.0, 1E-6, true, true, new LogisticGradient(true, true, 1.0), new SquaredL2Updater)

  /**
    * calculates the mean and variance of incoming dataset and update the existing one
    *
    * @param data incoming rdd to update the statistics
    */
  override def updateStatistics(data: RDD[(Double, Vector)]) = {
    val newBatchSummarizer = {
      val seqOp = (c: (MultivariateOnlineSummarizer), instance: (Double, Vector)) =>
        c.add(instance._2)

      val combOp = (c1: (MultivariateOnlineSummarizer),
                    c2: (MultivariateOnlineSummarizer)) => c1.merge(c2)

      data.treeAggregate(new MultivariateOnlineSummarizer)(seqOp, combOp)
    }
    summarizer = if (summarizer == null) {
      logger.info("Calculating the statistics for the first time !!")
      newBatchSummarizer
    } else {
      summarizer.merge(newBatchSummarizer)
    }
    numFeatures = summarizer.mean.size
    featuresMean = summarizer.mean.toArray
    featuresStd = summarizer.variance.toArray.map(math.sqrt)
  }

  override def getStatistics(statisticsType: String) = {
    statisticsType match {
      case "mean" => featuresMean
      case "std" => featuresStd
      case "size" => Array(numFeatures)
    }
  }

  override def unStandardize(weights: Vector): Vector = {
    val rawCoefficients = weights.toArray.clone()
    var i = 0
    while (i < rawCoefficients.length - 1) {
      rawCoefficients(i) *= {
        if (featuresStd(i) != 0.0) 1.0 / featuresStd(i) else 0.0
      }
      i += 1
    }
    new DenseVector(rawCoefficients)
  }


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
    GradientDescent.runMiniBatchSGD(
      data,
      gradient,
      updater,
      stepSize,
      numIterations,
      regParam,
      miniBatchFraction,
      initialWeights,
      convergenceTol,
      standardize,
      fitIntercept,
      intercept)
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
                      miniBatchFraction: Double,
                      initialWeights: Vector,
                      convergenceTol: Double,
                      standardization: Boolean,
                      fitIntercept: Boolean,
                      intercept: Double): Vector = {

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

    val numExamples = data.count()

    // if no data, return initial weights to avoid NaNs
    if (numExamples == 0) {
      logger.warn("GradientDescent.runMiniBatchSGD returning initial weights, no data found")
      return initialWeights
    }

    if (numExamples * miniBatchFraction < 1) {
      logger.warn("The miniBatchFraction is too small")
    }

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

    if (!data.getStorageLevel.useMemory) {
      logger.warn(s"Dataset is not persisted (${data.getStorageLevel.toString()})!!")
    }
    var converged = false
    var prevLoss = Double.MaxValue
    var currLoss = Double.MaxValue
    // indicates whether converged based on convergenceTol
    var i = 1
    while (!converged && i <= numIterations) {
      // this is to avoid the unnecessary sampling if sampling rate is 1.0
      val sampledData = if (miniBatchFraction == 1.0)
        data
      else
        data.sample(withReplacement = false, miniBatchFraction)

      val miniBatchSize = sampledData.count()
      prevLoss = currLoss
      val (lossSum, newGradients) = gradient.compute(sampledData, weights)

      previousWeights = Some(weights)
      // TODO: investigate whether or not the gradient should be divided by miniBatchSize
      // original code from spark divides the gradient by the mini batch size, but to me
      // it seems illogical and actually the opposite have to be done
      // investigate this and either add a bug report in spark or fix the implementation here
      val newParams = updater.compute(weights,
        LinearAlgebra.fromBreeze(newGradients / miniBatchSize.toDouble),
        stepSize, i, regParam)
      weights = newParams._1
      // divide loss by the mini batch size
      currLoss = lossSum / miniBatchSize.toDouble + newParams._2

      currentWeights = Some(weights)
      //      if (previousWeights.isDefined && currentWeights.isDefined) {
      //        converged = isConverged(previousWeights.get, currentWeights.get, convergenceTol)
      //      }
      converged = isConverged(prevLoss, currLoss, convergenceTol)
      logger.info(s"Iteration ($i/$numIterations) ,loss($currLoss)")
      i += 1
    }

    weights
  }


  private def isConverged(previousLoss: Double,
                          currentLoss: Double,
                          convergenceTol: Double): Boolean = {
    val diff = Math.abs(previousLoss - currentLoss)
    logger.info(s"diff($diff) and convergenceTol($convergenceTol)")
    diff < convergenceTol
  }

  private def isConverged(previousWeights: Vector,
                          currentWeights: Vector,
                          convergenceTol: Double): Boolean = {
    // To compare with convergence tolerance.
    val previousBDV = asBreeze(previousWeights).toDenseVector
    val currentBDV = asBreeze(currentWeights).toDenseVector

    // This represents the difference of updated weights in the iteration.
    val solutionVecDiff: Double = norm(previousBDV - currentBDV)

    val currentBDVNorm = norm(currentBDV)

    logger.info(s"diff($solutionVecDiff) and convergenceTol(${convergenceTol * Math.max(currentBDVNorm, 1.0)})")

    solutionVecDiff < convergenceTol * Math.max(norm(currentBDV), 1.0)
  }


}
