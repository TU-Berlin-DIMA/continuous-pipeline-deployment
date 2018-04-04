package de.dfki.ml.pipelines.urlrep

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.urlrep.GlobalVariables.URLRepRawType
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class URLRepMissingValueImputer() extends Component[URLRepRawType, URLRepRawType] {
  override def transform(spark: SparkContext, input: RDD[URLRepRawType]) = input

  override def update(sparkContext: SparkContext, input: RDD[URLRepRawType]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[URLRepRawType]) = {
    transform(spark, input)
  }
}
