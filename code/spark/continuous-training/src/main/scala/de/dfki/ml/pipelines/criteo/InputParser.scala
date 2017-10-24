package de.dfki.ml.pipelines.criteo

import de.dfki.ml.pipelines.Component
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import GLOBAL_VARIABLES._

/**
  * @author behrouz
  */
class InputParser(delim: String = "\t") extends Component[String, RawType] {
  override def transform(spark: SparkContext, input: RDD[String]): RDD[RawType] = {
    input map { line =>
      val features = line.split(delim, -1)
      val label = features.take(NUM_LABELS).head.toInt
      val numericalFeature = features.slice(NUM_LABELS, NUM_LABELS + NUM_INTEGER_FEATURES)
        .map(string => if (string.isEmpty) 0.0 else string.toDouble)
      val categoricalFeatures = features
        .slice(NUM_LABELS + NUM_INTEGER_FEATURES, NUM_FEATURES)

      // TODO: Check if this affects the performance (taken from Christoph Boden's code)
      // add dimension so that similar values in diff. dimensions get a different hash
      // however this may not be benifical for criteo as suggested by
      // http://fastml.com/vowpal-wabbit-eats-big-data-from-the-criteo-competition-for-breakfast/
      for (i <- categoricalFeatures.indices) {
        categoricalFeatures(i) = i + ":" + categoricalFeatures(i)
      }

      RawType(label, numericalFeature, categoricalFeatures)
    }
  }

  override def update(input: RDD[String]) = ???

  override def updateAndTransform(spark: SparkContext, input: RDD[String]) = ???
}
