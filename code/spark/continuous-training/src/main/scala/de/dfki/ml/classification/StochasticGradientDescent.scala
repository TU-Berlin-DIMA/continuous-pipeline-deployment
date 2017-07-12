package de.dfki.ml.classification

import de.dfki.ml.optimization.{BatchGradient, GradientDescent, SquaredL2Updater}
import org.apache.spark.SparkException
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, GeneralizedLinearModel, LabeledPoint}
import org.apache.spark.mllib.util.DataValidators
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

/**
  * @author behrouz
  */
abstract class StochasticGradientDescent[M <: GeneralizedLinearModel](val stepSize: Double,
                                                                      val numIterations: Int,
                                                                      val regParam: Double,
                                                                      val miniBatchFraction: Double,
                                                                      val standardization: Boolean,
                                                                      val fitIntercept: Boolean,
                                                                      val updater: Updater)
  extends GeneralizedLinearAlgorithm[M] {

  setIntercept(fitIntercept)

  def gradientFunction: BatchGradient

  override val optimizer = new GradientDescent(numIterations,
    stepSize,
    regParam,
    miniBatchFraction,
    1E-6,
    standardization,
    fitIntercept,
    gradientFunction,
    updater)

  override protected val validators = List(DataValidators.binaryLabelValidator)

  /**
    * run method that allows initial intercept
    *
    * @param input          input dataset
    * @param initialWeights initial weights
    * @param intercept      initial intercept
    * @return instance of model
    */
  def run(input: RDD[LabeledPoint], initialWeights: Vector, intercept: Double): M = {
    if (input.getStorageLevel == StorageLevel.NONE) {
      logWarning("The input data is not directly cached, which may hurt performance if its"
        + " parent RDDs are also uncached.")
    }

    // Check the data properties before running the optimizer
    if (validateData && !validators.forall(func => func(input))) {
      throw new SparkException("Input validation failed.")
    }


    val weightsWithIntercept = optimizer.optimize(input.map(l => (l.label, l.features)), initialWeights)


    val (weights, intercept) = if (!addIntercept) {
      (weightsWithIntercept, 0.0)
    } else {
      (Vectors.dense(weightsWithIntercept.toArray.dropRight(1)), weightsWithIntercept.toArray.last)
    }

    // Unpersist cached data
    if (input.getStorageLevel != StorageLevel.NONE) {
      input.unpersist(false)
    }
    createModel(weights, intercept)
  }
}
