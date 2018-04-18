package de.dfki.experiments

import de.dfki.ml.optimization.updater.{SquaredL2UpdaterWithAdam, Updater}

/**
  * @author behrouz
  */
case class Params(inputPath: String = "",
                  streamPath: String = "",
                  evaluationPath: String = "prequential",
                  var resultPath: String = "",
                  var initialPipeline: String = "",
                  delimiter: String = "",
                  numFeatures: Int = 100,
                  numIterations: Int = 500,
                  slack: Int = 5,
                  days: Array[Int] = Array(1, 2, 3, 4, 5),
                  sampleSize: Int = 100,
                  dayDuration: Int = 100,
                  pipelineName: String = "url-rep",
                  var regParam: Double = 0.001,
                  convergenceTol: Double = 1E-6,
                  miniBatch: Double = 0.1,
                  var stepSize: Double = 0.1,
                  var updater: Updater = new SquaredL2UpdaterWithAdam(),
                  var batchEvaluationSet: String = "") {

}
