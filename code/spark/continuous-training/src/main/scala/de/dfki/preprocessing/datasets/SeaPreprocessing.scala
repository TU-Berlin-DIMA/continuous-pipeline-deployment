package de.dfki.preprocessing.datasets

import java.io.{File, PrintWriter}

import de.dfki.classification.ContinuousClassifier
import de.dfki.preprocessing.parsers.CSVParser
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

import scala.io.Source

/**
  * Split the data into initial, streaming and test data
  *
  */

class SeaPreprocessing(sc: SparkContext = null, ratio: List[Double] = List(0.1, 0.9)) {


  def fromFile(path: String): (RDD[LabeledPoint], RDD[LabeledPoint]) = {
    val rdd = sc.textFile(path).map(new CSVParser().parsePoint)
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
    val f = new File("data/sea/initial-training/init")
    f.getParentFile.mkdirs()
    val writer = new PrintWriter(f)
    writer.write(initial.mkString("\n"))
    writer.close()

    val streamingBatchSize = 90
    var i = 0
    while (i < 600) {
      val dataBatch = streaming.slice(i * streamingBatchSize, (i + 1) * streamingBatchSize)
      val file = new File(s"data/sea/stream-training/stream$i")
      file.getParentFile.mkdir()
      val writer = new PrintWriter(file)
      writer.write(dataBatch.mkString("\n"))
      writer.close()
      i = i + 1
    }

  }

}

object SeaPreprocessing {
  def main(args: Array[String]): Unit = {
    val d = new SeaPreprocessing()
    d.sequentialSplitter("data/sea/raw/sea.data")

  }
}
