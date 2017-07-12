package de.dfki.ml.classification

import de.dfki.ml.optimization.{GradientDescent, LogisticGradient, SquaredL2Updater}
import org.apache.spark.SparkException
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, LabeledPoint}
import org.apache.spark.mllib.util.DataValidators
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

/**
  * @author bede01.
  */
class LogisticRegression {

}

class LogisticRegressionWithSGD(val stepSize: Double,
                                val numIterations: Int,
                                val regParam: Double,
                                val miniBatchFraction: Double,
                                val standardization: Boolean,
                                val fitIntercept: Boolean,
                                val updater: Updater)
  extends GeneralizedLinearAlgorithm[LogisticRegressionModel] with Serializable {

  override val optimizer = new GradientDescent(numIterations,
    stepSize,
    regParam,
    miniBatchFraction,
    1E-6,
    standardization,
    fitIntercept,
    new LogisticGradient(fitIntercept, standardization, regParam),
    updater)

  override protected val validators = List(DataValidators.binaryLabelValidator)

  def this() = this(1.0, 100, 0.1, 1.0, true, true, new SquaredL2Updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, 1.0, true, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double,
           updater: Updater) = this(stepSize, numIterations, regParam, miniBatchFraction, true, true, updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double) = this(stepSize, numIterations, regParam, miniBatchFraction, true, true, new SquaredL2Updater)

  override def createModel(weights: Vector, intercept: Double) = {
    println(s"Creating a model with features size: ${weights.size} and intercept: $intercept")
    new LogisticRegressionModel(weights, intercept)
  }


  override def run(input: RDD[LabeledPoint], initialWeights: Vector): LogisticRegressionModel = {
    run(input, initialWeights, 0.0)
  }

  def run(input: RDD[LabeledPoint], initialWeights: Vector, intercept: Double): LogisticRegressionModel = {
    if (input.getStorageLevel == StorageLevel.NONE) {
      logWarning("The input data is not directly cached, which may hurt performance if its"
        + " parent RDDs are also uncached.")
    }

    // Check the data properties before running the optimizer
    if (validateData && !validators.forall(func => func(input))) {
      throw new SparkException("Input validation failed.")
    }


    val weightsWithIntercept = optimizer.optimize(input.map(l => (l.label, l.features)), initialWeights)


    val (weights, intercept) = if (!fitIntercept) {
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
