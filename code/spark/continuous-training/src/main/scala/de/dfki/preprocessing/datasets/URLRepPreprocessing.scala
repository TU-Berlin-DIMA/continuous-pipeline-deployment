package de.dfki.preprocessing.datasets

import de.dfki.preprocessing.Preprocessor
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author bede01
  */
object URLRepPreprocessing {
  val INPUT_PATH = "data/url-reputation/raw"
  val OUTPUT_PATH = "data/url-reputation-sample"
  val FILE_COUNT = 2
  val SAMPLING_RATE = 0.05

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("URL Data")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.set("spark.hadoop.validateOutputSpecs", "false")
    conf.setMaster(masterURL)
    val preprocessor = new Preprocessor()
    val sc = new SparkContext(conf)
    val inputPath = parser.get("input-path", INPUT_PATH)
    val outputPath = parser.get("output-path", OUTPUT_PATH)
    val fileCount = parser.getInteger("file-count", FILE_COUNT)
    val samplingRate = parser.getDouble("sampling-rate", SAMPLING_RATE)
    val data = MLUtils.loadLibSVMFile(sc, s"$inputPath/Day0.svm")
      .sample(withReplacement = false, fraction = samplingRate, seed = 42)
      .map(l => new LabeledPoint(if (l.label == -1.0) 0 else 1.0, l.features))
    preprocessor.convertToSVM(data).saveAsTextFile(s"$outputPath/initial-training/")


    for (i <- 1 until 120) {
      val data = MLUtils.loadLibSVMFile(sc, s"$inputPath/Day$i.svm")
        .map(l => new LabeledPoint(if (l.label == -1.0) 0 else 1.0, l.features))
      val hadoopConf = new Configuration()
      hadoopConf.set("mapreduce.output.basename", s"day${"%05d".format(i)}")
      preprocessor.convertToSVM(data).repartition(fileCount)
        .sample(withReplacement = false, fraction = samplingRate, seed = 42)
        .map(str => (null, str)).saveAsNewAPIHadoopFile(s"$outputPath/stream-training/", classOf[NullWritable], classOf[String],
        classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
    }
  }


}