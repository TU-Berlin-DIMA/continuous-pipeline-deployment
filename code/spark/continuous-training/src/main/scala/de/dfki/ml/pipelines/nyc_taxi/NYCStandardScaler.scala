package de.dfki.ml.pipelines.nyc_taxi

import de.dfki.ml.pipelines.Component
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{DenseVector, Vector}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.MultivariateOnlineSummarizer
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class NYCStandardScaler extends Component[LabeledPoint, LabeledPoint] {

  var summarizer: MultivariateOnlineSummarizer = _
  var featuresMean: Array[Double] = _
  var featuresStd: Array[Double] = _

  override def transform(spark: SparkContext, input: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    val broadcastMean = spark.broadcast(featuresMean)
    val broadcastStd = spark.broadcast(featuresStd)

    input.mapPartitions { iter =>
      val fm = broadcastMean.value
      val fs = broadcastStd.value
      iter.map {
        row =>
          val scaledValues = (row.features.toArray, fm, fs).zipped.toList.map {
            case (feature, mean, standardDeviation) => (feature - mean) / standardDeviation
          }.toArray
          LabeledPoint(row.label, new DenseVector(scaledValues))
      }
    }
  }

  override def update(spark: SparkContext, input: RDD[LabeledPoint]) = {
    val newBatchSummarizer = {
      val seqOp = (c: (MultivariateOnlineSummarizer), instance: (Vector)) =>
        c.add(instance)

      val combOp = (c1: (MultivariateOnlineSummarizer),
                    c2: (MultivariateOnlineSummarizer)) => c1.merge(c2)

      input.map(d => d.features).treeAggregate(new MultivariateOnlineSummarizer)(seqOp, combOp)
    }
    summarizer = if (summarizer == null) {
      newBatchSummarizer
    } else {
      summarizer.merge(newBatchSummarizer)
    }

    featuresMean = summarizer.mean.toArray
    // TODO: find a proper solution . maybe remove the column with constant values
    featuresStd = summarizer.variance.toArray.map(math.sqrt).map(sd => if (sd == 0.0) 1.0 else sd)
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[LabeledPoint]) = {
    update(spark, input)
    transform(spark, input)
  }
}
