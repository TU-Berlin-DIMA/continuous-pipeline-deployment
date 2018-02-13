package de.dfki.ml.pipelines.urlrep

import java.nio.charset.Charset

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.urlrep.GlobalVariables._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.util.sketch.BloomFilter

import scala.util.hashing.MurmurHash3

/**
  * @author behrouz
  */
class URLRepOneHotEncoder(val numCategories: Int) extends Component[URLRepRawType, LabeledPoint] {
  var encoding: RDD[(String, Int)] = _
  var approximateFeatureSize = 0L
  var bloomFilter = BloomFilter.create(numCategories, 0.1)

  override def transform(spark: SparkContext, input: RDD[URLRepRawType]): RDD[LabeledPoint] = {
    input.map {
      item =>
        val encodedIndices = item.categorical.map {
          cat =>
            // skip the training instance one of the categorical variables do not exist in the map
            murmurHash(cat, numCategories) + NUM_REAL_FEATURES
        }.distinct
        val categoricalVector = new SparseVector(
          numCategories + NUM_REAL_FEATURES,
          (0 until NUM_REAL_FEATURES).toArray ++ encodedIndices.sorted,
          item.numerical ++ Array.fill[Double](encodedIndices.length)(1.0)
        )
        new LabeledPoint(item.label, categoricalVector)
    }
  }

  override def update(spark: SparkContext, input: RDD[URLRepRawType]) = {
    val filter = spark.broadcast(bloomFilter)
    val uniques = input
      .flatMap(_.categorical)
      .distinct()
      .repartition(20)
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
    }.collect()
    bloomFilter = updatedFilters.reduce((b1, b2) => b1.mergeInPlace(b2))
  }

  private def murmurHash(feature: String, numFeatures: Int): Int = {
    val hash = MurmurHash3.bytesHash(feature.getBytes(Charset.forName("UTF-8")), 0)
    scala.math.abs(hash) % numFeatures
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[URLRepRawType]): RDD[LabeledPoint] = {
    update(spark, input)
    transform(spark, input)
  }
}
