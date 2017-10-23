package de.dfki.ml.pipelines.criteo

import de.dfki.ml.pipelines.Component
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import GLOBAL_VARIABLES._

/**
  * @author behrouz
  */
class InputParser(delim: String = ",") extends Component[String, RawType] {
  override def transform(spark: SparkContext, input: RDD[String]): RDD[RawType] = {
    input map { line =>
      val features = line.split(delim, -1)
      val label = features.take(NUM_LABELS).head.toInt
      val numericalFeature = features.slice(NUM_LABELS, NUM_LABELS + NUM_INTEGER_FEATURES)
        .map(string => if (string.isEmpty) 0.0 else string.toDouble)
      val categoricalFeatures = features
        .slice(NUM_LABELS + NUM_INTEGER_FEATURES, NUM_FEATURES)

      RawType(label, numericalFeature, categoricalFeatures)
    }
  }

  override def update(input: RDD[String]) = ???

  override def updateAndTransform(spark: SparkContext, input: RDD[String]) = ???
}
