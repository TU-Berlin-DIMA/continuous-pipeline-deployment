package de.dfki.experiments

/**
  * @author behrouz
  */
case class Params(inputPath: String = "",
                  streamPath: String = "",
                  evaluationPath: String = "prequential",
                  resultPath: String = "",
                  initialPipeline: String = "",
                  delimiter: String = "",
                  numFeatures: Int = 100,
                  numIterations: Int = 500,
                  slack: Int = 5,
                  days: Array[Int] = Array(1, 2, 3, 4, 5),
                  sampleSize: Int = 100,
                  dayDuration: Int = 100,
                  pipelineName: String = "url-rep",
                  regParam: Double = 0.001) {

}
