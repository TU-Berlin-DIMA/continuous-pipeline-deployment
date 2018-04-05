package de.dfki.ml.pipelines.urlrep

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.urlrep.GlobalVariables.{URLRepRawType, _}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class URLRepSVMParser() extends Component[String, URLRepRawType] {

  override def transform(spark: SparkContext, input: RDD[String]): RDD[URLRepRawType] = {
    input map { line =>
      val items = line.split(' ')
      // svm labels are -1/1
      val label = if (items.head.toDouble == -1) 0 else 1
      val values = items.tail.filter(_.nonEmpty).map { item =>
        val indexAndValue = item.split(':')
        val index = indexAndValue(0).toInt
        val value = indexAndValue(1).toDouble
        (index, value)
      }.toMap

      val numerical = URLRepRealIndices.map { i => values.getOrElse(i, 0.0) }
      val categorical = values.filter { kv => !URLRepRealIndices.contains(kv._1) }.keys.toArray.map(_.toString)

      URLRepRawType(label, numerical, categorical)
    }
  }

  override def update(sparkContext: SparkContext, input: RDD[String]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[String]) = {
    transform(spark, input)
  }
}
