package de.dfki.streaming.models


import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.streaming.dstream.DStream

/**
  * @author Behrouz Derakhshan
  */
class HybridSVM(private var stepSize: Double,
                private var numIterations: Int,
                private var miniBatchFraction: Double,
                private var regParam: Double)
  extends HybridModel[SVMModel, SVMWithSGD] {


  override protected var model: Option[SVMModel] = None
  override protected val algorithm: SVMWithSGD = new SVMWithSGD()


  def this() = this(0.1, 50, 1.0, 0.0)



  override def trainOn(observations: DStream[LabeledPoint]): Unit = {
    super.trainOn(observations)
  }

  override def toString(): String = {
    model.get.toString()
  }


}
