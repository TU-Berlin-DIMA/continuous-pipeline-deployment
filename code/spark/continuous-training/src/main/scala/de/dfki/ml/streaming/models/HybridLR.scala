package de.dfki.ml.streaming.models

import de.dfki.ml.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.LogisticRegressionModel

/**
  * @author bede01.
  */
class HybridLR(private var stepSize: Double,
               private var numIterations: Int,
               private var miniBatchFraction: Double,
               private var regParam: Double)
  extends HybridModel[LogisticRegressionModel, LogisticRegressionWithSGD] {


  /**
    * Construct a StreamingLogisticRegression object with default parameters:
    * {stepSize: 0.1, numIterations: 50, miniBatchFraction: 1.0, regParam: 0.0}.
    * Initial weights must be set before using trainOn or predictOn
    * (see `StreamingLinearAlgorithm`)
    */
  def this() = this(0.1, 50, 1.0, 0.0)

  protected val algorithm = new LogisticRegressionWithSGD()

  protected var model: Option[LogisticRegressionModel] = _

  /**
    * Set the convergence tolerance
    *
    * @param convergenceTol convergence toll
    * @return
    */
  override def setConvergenceTol(convergenceTol: Double): this.type = {
    this.algorithm.optimizer.setConvergenceTol(convergenceTol)
    this
  }


}
