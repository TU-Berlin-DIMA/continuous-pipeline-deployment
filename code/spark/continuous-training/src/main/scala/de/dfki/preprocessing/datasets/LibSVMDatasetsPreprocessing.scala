package de.dfki.preprocessing.datasets

import de.dfki.preprocessing.Preprocessor
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author behrouz
  */
abstract class LibSVMDatasetsPreprocessing extends Serializable{

  var input: String = _
  var output: String = _
  var fileCount: Int = _
  var samplingRate: Double = _
  var outputFormat: String = _
  var shouldScale: Boolean = _

  def process(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    input = parser.get("input-path", defaultInputPath)
    output = parser.get("output-path", defaultOutputPath)
    fileCount = parser.getInteger("file-count", defaultFileCount)
    samplingRate = parser.getDouble("sampling-rate", defaultSamplingRate)
    shouldScale = parser.getBoolean("scale", defaultScale)
    outputFormat = parser.get("output-format", defaultOutputFormat)

    val conf = new SparkConf().setAppName("LIBSVM Data Set Preprocessing")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    val trainData = MLUtils.loadLibSVMFile(sc, input).map(lp => {
      new LabeledPoint(mapLabel(lp.label), lp.features)
    })

    val data = sample(trainData, samplingRate)

    val scaledData = scale(data)

    val outputData = formatOutput(scaledData)
    val splits = Preprocessor.split(outputData, Array(0.1, 0.9))
    splits._1.saveAsTextFile(s"$output/initial-training/")
    splits._2.repartition(fileCount).saveAsTextFile(s"$output/stream-training/")
  }

  def sample(data: RDD[LabeledPoint], samplingRate: Double): RDD[LabeledPoint] = {
    if (samplingRate < 1.0)
      data.sample(withReplacement = false, fraction = samplingRate, seed = 42)
    else
      data
  }

  def scale(data: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    if (shouldScale)
      Preprocessor.scale(data.map(a => new LabeledPoint(a.label, new DenseVector(a.features.toArray))))
    else
      data
  }


  def formatOutput(data: RDD[LabeledPoint]): RDD[String] = {
    if (outputFormat == "svm")
      Preprocessor.convertToSVM(data)
    else
      Preprocessor.convertToCSV(data)

  }

  def mapLabel(label: Double): Double

  def defaultInputPath: String

  def defaultOutputPath: String

  def defaultFileCount: Int

  def defaultSamplingRate: Double

  def defaultOutputFormat: String

  def defaultScale: Boolean

}
