package de.dfki.preprocessing

import java.io.{File, PrintWriter}

import de.dfki.classification.ContinuousClassifier
import de.dfki.utils.MLUtils
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import scala.io.Source

/**
  * Split the data into initial, streaming and test data
  *
  */

class DataSplitter(sc: SparkContext = null, ratio: List[Double] = List(0.1, 0.8)) {


  def fromFile(path: String): (RDD[LabeledPoint], RDD[LabeledPoint]) = {
    val rdd = sc.textFile(path).map(MLUtils.parsePoint)
    fromRDD(rdd)
  }

  def fromRDD(rdd: RDD[LabeledPoint]) = {
    val rdds = rdd.randomSplit(ratio.toArray)
    (rdds(0), rdds(1))
  }

  def sequentialSplitter(path: String): Unit = {
    val lines = Source.fromFile(path).getLines().toList
    val initialCutOff = 6000
    val (initial, streaming) = lines.splitAt(initialCutOff)
    print(initial.size)
    val f = new File(s"${ContinuousClassifier.BASE_DATA_DIRECTORY}/${ContinuousClassifier.INITIAL_TRAINING}/init")
    f.getParentFile.mkdirs()
    val writer = new PrintWriter(f)
    writer.write(initial.mkString("\n"))
    writer.close()

    val streamingBatchSize = 540
    var i = 0
    while (i < 100) {
      val dataBatch = streaming.slice(i * streamingBatchSize, (i + 1) * streamingBatchSize)
      val file = new File(s"${ContinuousClassifier.BASE_DATA_DIRECTORY}/${ContinuousClassifier.STREAM_TRAINING}/stream$i")
      file.getParentFile.mkdir()
      val writer = new PrintWriter(file)
      writer.write(dataBatch.mkString("\n"))
      writer.close()
      i = i + 1
    }

  }

}

object DataSplitter {
  def main(args: Array[String]): Unit = {
    val d = new DataSplitter()
    d.sequentialSplitter("data/sea/raw/sea.data")

  }
}
