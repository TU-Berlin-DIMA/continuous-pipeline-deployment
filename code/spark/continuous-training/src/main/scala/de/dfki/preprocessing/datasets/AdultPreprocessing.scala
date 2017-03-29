package de.dfki.preprocessing.datasets

/**
  * @author behrouz
  */
object AdultPreprocessing extends LibSVMDatasetsPreprocessing {

  def main(args: Array[String]): Unit = {
    process(args)
  }

  override def defaultInputPath = "data/adult/raw"

  override def defaultOutputPath = "data/adult"

  override def defaultFileCount = 100

  override def defaultSamplingRate = 1.0

  override def defaultOutputFormat = "svm"

  override def defaultScale = false

  override def mapLabel(label: Double) = if (label == -1.0) 0.0 else label
}
