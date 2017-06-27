package de.dfki.examples


import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
object LogisticRegressionTest {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Logistic Regression").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc, "data/cover-types/libsvm/")
      .map(l => LabeledPoint(l.label - 1, l.features))

    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)


    val model = new LogisticRegressionWithSGD().run(training)


    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    val metrics = new BinaryClassificationMetrics(predictionAndLabels)
    val auc = metrics.areaUnderROC()
    println(s"AUC = $auc")

  }

}
