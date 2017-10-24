package de.dfki.ml.pipelines.criteo

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.criteo.GLOBAL_VARIABLES._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
  * @author behrouz
  */
class OneHotEncoder extends Component[RawType, LabeledPoint] {
  var encoding: mutable.HashMap[String, Long] = mutable.HashMap.empty[String, Long]
  var dimension: Int = 0

  override def transform(spark: SparkContext, input: RDD[RawType]): RDD[LabeledPoint] = {
    val broadCastEncoding = spark.broadcast(encoding)
    input.map {
      item =>
        val mapping = broadCastEncoding.value
        val size = mapping.size
        val encodedIndices = item.categorical.flatMap {
          item =>
            // skip the training instance one of the categorical variables do not exist in the map
            val index = mapping.get(item)
            if (index.isEmpty) {
              Seq()
            } else {
              Seq(index.get.toInt)
            }
        }.distinct
        val categoricalVector = new SparseVector(
          size + NUM_INTEGER_FEATURES,
          (0 to 12).toArray ++ encodedIndices.sorted,
          item.numerical ++ Array.fill[Double](encodedIndices.length)(1.0)
        )
        new LabeledPoint(item.label, categoricalVector)
    }
  }

  override def update(input: RDD[RawType]) = {
    val distinct = input
      .flatMap(_.categorical)
      .distinct()
      .zipWithIndex()
      .collectAsMap()


    if (encoding.isEmpty) {
      encoding ++= mutable.HashMap[String, Long](
        distinct
          // increment by the number of integer features
          .map(item => (item._1, item._2 + NUM_INTEGER_FEATURES))
          .toArray: _*
      )
    } else {
      var size = encoding.size
      for ((k, v) <- distinct) {
        if (encoding.get(k).isEmpty) {
          // if key does not exist, append the new feature to the end
          encoding += (k -> (size + NUM_INTEGER_FEATURES))
          // increment the size parameter so the indexes don't clash
          size += 1
        }
      }
    }
    // new feature dimension size
    dimension = encoding.size + NUM_INTEGER_FEATURES
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[RawType]): RDD[LabeledPoint] = {
    update(input)
    transform(spark, input)
  }

  def getCurrentDimension = dimension

}
