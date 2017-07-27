package de.dfki.ml.streaming.models


import de.dfki.ml.classification.SVMWithSGD
import de.dfki.ml.optimization.SquaredL2Updater
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.optimization.Updater
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.streaming.dstream.DStream

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


  override def trainOn(observations: DStream[LabeledPoint]): Unit = {
    super.trainOn(observations)
  }

  override val getType = "svm"
}
