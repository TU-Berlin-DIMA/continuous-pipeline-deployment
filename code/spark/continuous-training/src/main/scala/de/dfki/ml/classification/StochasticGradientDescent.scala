package de.dfki.ml.classification

import de.dfki.ml.optimization.updater.Updater
import de.dfki.ml.optimization.GradientDescent
import de.dfki.ml.optimization.gradient.BatchGradient
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, GeneralizedLinearModel, LabeledPoint}
import org.apache.spark.mllib.util.DataValidators
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

/**
  * TODO: This class can be removed and the methods refactored
  *
  * @author behrouz
  */
abstract class StochasticGradientDescent[M <: GeneralizedLinearModel](val stepSize: Double,
                                                                      val numIterations: Int,
                                                                      val regParam: Double,
                                                                      val convergenceTol: Double,
                                                                      val miniBatchFraction: Double,
                                                                      val fitIntercept: Boolean,
                                                                      val updater: Updater) extends GeneralizedLinearAlgorithm[M] {

  setIntercept(fitIntercept)
  def gradientFunction: BatchGradient

  override val optimizer = new GradientDescent(stepSize = stepSize,
    numIterations = numIterations,
    regParam = regParam,
    convergenceTol = convergenceTol,
    miniBatchFraction = miniBatchFraction,
    fitIntercept = fitIntercept,
    gradient = gradientFunction,
    updater = updater)

  override protected val validators = List(DataValidators.binaryLabelValidator)

  /**
    * run method that allows initial intercept
    *
    * @param input            input dataset
    * @param initialWeights   initial weights
    * @param initialIntercept initial intercept
    * @return instance of model
    */
  def run(input: RDD[LabeledPoint], initialWeights: Vector, initialIntercept: Double): M = {
    if (input.getStorageLevel == StorageLevel.NONE) {
      logWarning("The input data is not directly cached, which may hurt performance if its"
        + " parent RDDs are also uncached.")
    }


    val weightsWithIntercept = optimizer.optimize(input.map(l => (l.label, l.features)), initialWeights, initialIntercept)


    val (weights, intercept) = if (!addIntercept) {
      (weightsWithIntercept, 0.0)
    } else {
      (Vectors.dense(weightsWithIntercept.toArray.dropRight(1)), weightsWithIntercept.toArray.last)
    }

    createModel(weights, intercept)
  }

  def getConvergedAfter = this.optimizer.getConvergedAfter

  override def toString = {
    s"${this.getClass.getName} with Params: " +
      s"stepSize($stepSize), numIterations($numIterations), regParam($regParam)" +
      s", miniBatchFraction($miniBatchFraction), fitIntercept($fitIntercept)" +
      s", updater(${updater.toString})"
  }
}
