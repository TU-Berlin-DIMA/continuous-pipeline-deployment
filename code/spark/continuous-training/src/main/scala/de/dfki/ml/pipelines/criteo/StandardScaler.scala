package de.dfki.ml.pipelines.criteo

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.criteo.GLOBAL_VARIABLES.RawType
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.MultivariateOnlineSummarizer
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class StandardScaler extends Component[RawType, RawType] {
  var summarizer: MultivariateOnlineSummarizer = _
  var featuresMean: Array[Double] = _
  var featuresStd: Array[Double] = _

  override def transform(spark: SparkContext, input: RDD[RawType]): RDD[RawType] = {
    val broadcastMean = spark.broadcast(featuresMean)
    val broadcastStd = spark.broadcast(featuresStd)
    input.map {
      row =>
        val scaledValues = (row.numerical, broadcastMean.value, broadcastStd.value).zipped.toList.map {
          case (feature, mean, standardDeviation) => (feature - mean) / standardDeviation
        }.toArray
        RawType(row.label, scaledValues, row.categorical)
    }
  }

  override def update(input: RDD[RawType]) = {
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
    featuresStd = summarizer.variance.toArray.map(math.sqrt)
  }

  override def updateAndTransform(spark: SparkContext, input: RDD[RawType]) = {
    update(input)
    transform(spark,input)
  }
}
