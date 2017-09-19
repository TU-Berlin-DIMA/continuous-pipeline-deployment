package de.dfki.deployment

import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Train on full data set and show the overall error rate on test set
  *
  * @author Behrouz Derakhshan
  */
object BatchClassifier extends Classifier {

  def main(args: Array[String]) {
    run(args)
  }

  override def run(args: Array[String]): Unit = {
    parseArgs(args)
    val conf = new SparkConf().setMaster("local[*]").setAppName("Batch SVM Classifier")
    val sc = new SparkContext(conf)

    val trainingRDD = sc.textFile(initialDataPath + "," + streamingDataPath).map(dataParser.parsePoint).cache()

    val testRDD = sc.textFile(evaluationDataPath).map(dataParser.parsePoint)

    val model = SVMWithSGD.train(trainingRDD, 100)

    val errorRate = testRDD.map(t => (model.predict(t.features), t.label))
      .map(a => {
        if (a._1 == a._2) {
          (0.0, 1.0)
        }
        else {
          (1.0, 1.0)
        }
      }).reduce((a, b) => (a._1 + b._1, a._2 + b._2))

    println(s"Error rate = ${errorRate._1 / errorRate._2}")
  }

  override def parseArgs(args: Array[String]) = super.parseArgs(args)

  override def getApplicationName = "Batch SVM Model"

  override def getExperimentName = "batch"

  override def defaultBatchDuration = 0L

  override def defaultTrainingSlack = 0L

  override def defaultModelType = "svm"
}

//Error rate = 0.3145537463802043