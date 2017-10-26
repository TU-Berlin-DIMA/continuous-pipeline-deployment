package de.dfki.ml.pipelines

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
trait Component[I, O] extends Serializable {
  def transform(spark: SparkContext, input: RDD[I]): RDD[O]

  def update(sparkContext: SparkContext, input: RDD[I])

  def updateAndTransform(spark: SparkContext, input: RDD[I]): RDD[O]
}
