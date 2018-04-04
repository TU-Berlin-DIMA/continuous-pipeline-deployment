package de.dfki.ml.pipelines.nyc_taxi


import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.nyc_taxi.GLOBAL_VARIABLES.NYCTaxiFeatures
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint

import math._

/**
  * @author behrouz
  */
class NYCAnomalyDetector extends Component[NYCTaxiFeatures, LabeledPoint] {

  // check if the pickup drop off are within boundaries of newyork
  override def transform(spark: SparkContext, input: RDD[NYCTaxiFeatures]) = {
    input.filter { record =>
      (record.tripDuration < 22 * 3600 && record.tripDuration > 10) &&
        record.distance > 0
    }.map { record =>
      val vector = new DenseVector(record.features())
      new LabeledPoint(log10(record.tripDuration), vector)
    }
  }

  override def update(sparkContext: SparkContext, input: RDD[NYCTaxiFeatures]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[NYCTaxiFeatures]) = {
    transform(spark, input)
  }
}
