package de.dfki.preprocessing.datasets

/**
  * @author behrouz derakhshan
  */
object CoverTypePreprocesing extends LibSVMDatasetsPreprocessing {

  def main(args: Array[String]): Unit = {
    process(args)
  }

  override def defaultInputPath = "data/cover-types/libsvm"

  override def defaultOutputPath = "data/cover-types/"

  override def defaultFileCount = 100

  override def defaultSamplingRate = 1.0

  override def defaultOutputFormat = "csv"

  override def defaultScale = true

  override def mapLabel(label: Double) = label - 1
}
