package de.dfki.ml.streaming.models


import de.dfki.ml.classification.SVMWithSGD
import de.dfki.ml.optimization.updater.{SquaredL2Updater, Updater}
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vector


/**
  * @author Behrouz Derakhshan
  */
class HybridSVM(private var stepSize: Double,
                private var numIterations: Int,
                private var regParam: Double,
                private var miniBatchFraction: Double,
                private var updater: Updater)
  extends HybridModel[SVMModel, SVMWithSGD] {


  override protected var model: Option[SVMModel] = None
  override protected val algorithm: SVMWithSGD = new SVMWithSGD()


  def this() = this(0.1, 50, 1.0, 0.0, new SquaredL2Updater)


  override val getType = "svm"

  // TODO: Implement this
  override def predictPoint(data: Vector, weight: Vector, intercept: Double) = ???

}
