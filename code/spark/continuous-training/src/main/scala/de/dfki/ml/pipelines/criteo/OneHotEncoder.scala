package de.dfki.ml.pipelines.criteo

import java.nio.charset.Charset

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.criteo.GLOBAL_VARIABLES._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.util.sketch.BloomFilter

import scala.util.hashing.MurmurHash3

/**
  * @author behrouz
  */
class OneHotEncoder(val numCategories: Int) extends Component[RawType, LabeledPoint] {
  var encoding: RDD[(String, Int)] = _
  var approximateFeatureSize = 0L
  var bloomFilter = BloomFilter.create(numCategories, 0.1)

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

  override def update(spark: SparkContext, input: RDD[RawType]) = {
    val filter = spark.broadcast(bloomFilter)
    val uniques = input
      .flatMap(_.categorical)
      .distinct()
      .cache()
    val size = uniques.count()
    if (approximateFeatureSize == 0) {
      // bloom filter is empty, update the count from the exact value
      approximateFeatureSize = size
    }
    else {
      approximateFeatureSize += uniques.filter(!filter.value.mightContainString(_)).count()
    }
    // update the bloomfilter
    val updatedFilters = uniques.mapPartitions {
      partition =>
        val curFilter = filter.value
        for (s <- partition)
          curFilter.putString(s)
        Seq(curFilter).iterator
    }
    bloomFilter = updatedFilters.treeReduce((b1, b2) => b1.mergeInPlace(b2))
    uniques.unpersist(false)
    filter.unpersist(false)
  }

  private def murmurHash(feature: String, numFeatures: Int): Int = {
    val hash = MurmurHash3.bytesHash(feature.getBytes(Charset.forName("UTF-8")), 0)
    scala.math.abs(hash) % numFeatures
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[RawType]): RDD[LabeledPoint] = {
    update(spark, input)
    transform(spark, input)
  }
}
