package de.dfki.ml.classification

import de.dfki.ml.optimization.gradient.HingeGradient
import de.dfki.ml.optimization.updater.{SquaredL2Updater, Updater}
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD


/**
  * @author behrouz
  */
class SVMWithSGD(stepSize: Double,
                 numIterations: Int,
                 regParam: Double,
                 convergenceTol: Double,
                 miniBatchFraction: Double,
                 fitIntercept: Boolean,
                 updater: Updater)
  extends StochasticGradientDescent[SVMModel](stepSize,
    numIterations,
    regParam,
    convergenceTol,
    miniBatchFraction,
    fitIntercept,
    updater) with Serializable {

  def this() = this(1.0, 100, 0.1, 1E-6, 1.0, true, new SquaredL2Updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           updater: Updater) = this(stepSize, numIterations, regParam,  1E-6, 1.0, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, 1E-6, miniBatchFraction, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double) = this(stepSize, numIterations, regParam, 1E-6, miniBatchFraction, true, new SquaredL2Updater)


  override def gradientFunction = new HingeGradient(fitIntercept, regParam)

  override protected def createModel(weights: Vector, intercept: Double): SVMModel = {
    //println(s"Creating a SVM model with features size: ${weights.size} and intercept: $intercept")
    new SVMModel(weights, intercept)
  }

  override def run(input: RDD[LabeledPoint], initialWeights: Vector): SVMModel = {
    run(input, initialWeights, 0.0)
  }


}
