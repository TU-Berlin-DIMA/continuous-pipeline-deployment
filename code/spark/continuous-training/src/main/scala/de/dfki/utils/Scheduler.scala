package de.dfki.utils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
  * Created by bede01 on 28/12/16.
  */
class Scheduler(sc: SparkContext) {


  var trainingData: RDD[LabeledPoint] = sc.emptyRDD[LabeledPoint]

  def append(data: RDD[LabeledPoint]): Unit = {
    trainingData = trainingData.union(data).cache()
  }

  def historicalData = {
    trainingData
  }

}
