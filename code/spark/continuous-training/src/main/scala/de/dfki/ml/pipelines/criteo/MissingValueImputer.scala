package de.dfki.ml.pipelines.criteo

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.criteo.GLOBAL_VARIABLES._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class MissingValueImputer extends Component[RawType, RawType] {
  override def transform(spark: SparkContext, input: RDD[RawType]) = {
    input.map {
      row =>
        val computed = row.categorical.map {
          v =>
            if (v.isEmpty) "Missing"
            else v
        }
        RawType(row.label, row.numerical, computed)
    }
  }

  override def update(input: RDD[RawType]) = {
    // do nothing
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[RawType]) = ???
}
