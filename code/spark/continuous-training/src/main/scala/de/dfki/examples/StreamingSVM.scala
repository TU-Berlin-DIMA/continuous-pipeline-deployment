package de.dfki.examples

import de.dfki.preprocessing.parsers.CSVParser
import de.dfki.ml.streaming.models.HybridSVM
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.Queue


/**
  * Testing a simple Streaming SVM model created by Apache Spark
  *
  * @author Behrouz
  */
object StreamingSVM {

  def main(args: Array[String]) {
    // train a logistic regression model
    val conf = new SparkConf().setMaster("local").setAppName("StreamingSVM")
    //val sc = new SparkContext(conf)
    val ssc = new StreamingContext(conf, Seconds(4))
    val sc = ssc.sparkContext
    val data = sc.textFile("data/test/")
      .map(new CSVParser().parsePoint)



    val model = SVMWithSGD.train(data, 100)
    // run it on a data stream

    val scoreAndLabels = data.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }
    //sc.stop()

    val streamingModel = new HybridSVM().setModel(model)
    val testRdd = ssc.sparkContext.textFile("data/test/").map(new CSVParser().parsePoint)
    val rddQueue = new Queue[RDD[LabeledPoint]]()

    val testData = ssc.queueStream(rddQueue, true, testRdd)

    streamingModel.predictOnValues(testData.map(lp => (lp.label, lp.features)))
      .map(a => {
        if (a._1 == a._2) {
          (0.0, 1.0)
        }
        else {
          (1.0, 1.0)
        }
      }).reduce((a, b) => (a._1 + b._1, a._2 + b._2))
      .map(item => "Error rate: " + item._1 / item._2)
      .print()


    ssc.start()
    ssc.awaitTermination()

  }


}
