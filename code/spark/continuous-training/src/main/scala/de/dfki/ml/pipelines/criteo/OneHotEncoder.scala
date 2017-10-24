package de.dfki.ml.pipelines.criteo

import java.nio.charset.Charset

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.criteo.GLOBAL_VARIABLES._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import scala.util.hashing.MurmurHash3

/**
  * @author behrouz
  */
class OneHotEncoder(val numCategories: Int) extends Component[RawType, LabeledPoint] {
  var encoding: RDD[(String, Int)] = _
  var dimension: Int = 0

  override def transform(spark: SparkContext, input: RDD[RawType]): RDD[LabeledPoint] = {

    input.map {
      item =>
        val encodedIndices = item.categorical.map {
          item =>
            // skip the training instance one of the categorical variables do not exist in the map
            murmurHash(item, numCategories) + NUM_INTEGER_FEATURES
        }.distinct
        val categoricalVector = new SparseVector(
          numCategories + NUM_INTEGER_FEATURES,
          (0 to 12).toArray ++ encodedIndices.sorted,
          item.numerical ++ Array.fill[Double](encodedIndices.length)(1.0)
        )
        new LabeledPoint(item.label, categoricalVector)
    }
  }

  override def update(input: RDD[RawType]) = {
//    val distinct = input
//      .flatMap(_.categorical)
//      .distinct()
//      .cache()
//
//
//    if (encoding.isEmpty) {
//      encoding = distinct.zipWithIndex.mapValues(_.toInt + NUM_INTEGER_FEATURES)
//      distinct.unpersist(false)
//    } else {
//      encoding.cache()
//      val size = encoding.count()
//      val diff = distinct.map((_, 0)).subtractByKey(encoding)
//      diff.zipWithIndex.mapValues(_ + size + NUM_INTEGER_FEATURES)
//      val combined = encoding.union(diff)
//      encoding.unpersist(true)
//      encoding = combined
//    }
//    // new feature dimension size
//    dimension = encoding.count.toInt + NUM_INTEGER_FEATURES
  }

  private def murmurHash(feature: String, numFeatures: Int): Int = {
    val hash = MurmurHash3.bytesHash(feature.getBytes(Charset.forName("UTF-8")), 0)
    scala.math.abs(hash) % numFeatures
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[RawType]): RDD[LabeledPoint] = {
    update(input)
    transform(spark, input)
  }

  def getCurrentDimension = dimension

}
