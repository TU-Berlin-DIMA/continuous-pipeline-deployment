package de.dfki.ml.pipelines.urlrep

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.urlrep.GlobalVariables.URLRepRawType
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.MultivariateOnlineSummarizer
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class URLRepStandardScaler extends Component[URLRepRawType, URLRepRawType] {
  var summarizer: MultivariateOnlineSummarizer = _
  var featuresMean: Array[Double] = _
  var featuresStd: Array[Double] = _

  override def transform(spark: SparkContext, input: RDD[URLRepRawType]): RDD[URLRepRawType] = {
    val broadcastMean = spark.broadcast(featuresMean)
    val broadcastStd = spark.broadcast(featuresStd)
    input.map {
      row =>
        val scaledValues = (row.numerical, broadcastMean.value, broadcastStd.value).zipped.toList.map {
          case (feature, mean, standardDeviation) => (feature - mean) / standardDeviation
        }.toArray
        URLRepRawType(row.label, scaledValues, row.categorical)
    }
  }

  override def update(spark: SparkContext, input: RDD[URLRepRawType]) = {
    val newBatchSummarizer = {
      val seqOp = (c: (MultivariateOnlineSummarizer), instance: (Vector)) =>
        c.add(instance)

      val combOp = (c1: (MultivariateOnlineSummarizer),
                    c2: (MultivariateOnlineSummarizer)) => c1.merge(c2)

      input.map(d => Vectors.dense(d.numerical)).treeAggregate(new MultivariateOnlineSummarizer)(seqOp, combOp)
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

  override def updateAndTransform(spark: SparkContext, input: RDD[URLRepRawType]) = {
    update(spark, input)
    transform(spark, input)
  }
}