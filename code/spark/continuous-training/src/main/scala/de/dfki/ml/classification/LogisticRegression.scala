package de.dfki.ml.classification

import de.dfki.ml.optimization.{GradientDescent, LogisticGradient, SquaredL2Updater}
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm
import org.apache.spark.mllib.util.DataValidators

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


  override val optimizer = new GradientDescent(new LogisticGradient(fitIntercept, standardization, regParam), updater)
    .setStepSize(stepSize)
    .setNumIterations(numIterations)
    .setRegParam(regParam)
    .setMiniBatchFraction(miniBatchFraction)
    .setConvergenceTol(1E-4)
    .setFitIntercept(fitIntercept)

  override protected val validators = List(DataValidators.binaryLabelValidator)

  def this() = this(1.0, 100, 0.1, 1.0, true, true, new SquaredL2Updater)

  def this(stepSize: Double,
           numIterations: Int,
           regParam: Double,
           miniBatchFraction: Double) = this(stepSize, numIterations, regParam, miniBatchFraction, true, true, new SquaredL2Updater)

  override def createModel(weights: Vector, intercept: Double) = {
    // TODO: This is a hack as I reusing the gradient update from the newer spark
    // package, I couldn't use the add intercept option provided in the GeneralizedLinearAlgorithm
    // class
    val (finalWeights, finalIntercept) = if (!fitIntercept) {
      (weights, intercept)
    } else {
      (Vectors.dense(weights.toArray.dropRight(1)), weights.toArray.last)
    }
    println(s"Creating a model with features size: ${finalWeights.size} and intercept: $finalIntercept")
    new LogisticRegressionModel(finalWeights, finalIntercept)
  }


}
