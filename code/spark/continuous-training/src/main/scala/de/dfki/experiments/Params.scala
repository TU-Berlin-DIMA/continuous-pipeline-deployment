package de.dfki.experiments

import de.dfki.ml.optimization.updater.{SquaredL2UpdaterWithAdam, Updater}

/**
  * @author behrouz
  */
case class Params(inputPath: String = "",
                  streamPath: String = "",
                  materializedPath:String = "",
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
                  var miniBatch: Double = 0.1,
                  var stepSize: Double = 0.1,
                  var updater: Updater = new SquaredL2UpdaterWithAdam(),
                  var batchEvaluationSet: String = "",
                  var numPartitions: Int,
                  var trainingFrequency: Int) {

  def copy(): Params = {
    Params(inputPath = inputPath,
      streamPath = streamPath,
      evaluationPath = evaluationPath,
      resultPath = resultPath,
      initialPipeline = initialPipeline,
      delimiter = delimiter,
      numFeatures = numFeatures,
      numIterations = numIterations,
      slack = slack,
      days = days,
      sampleSize = sampleSize,
      dayDuration = dayDuration,
      pipelineName = pipelineName,
      regParam = regParam,
      convergenceTol = convergenceTol,
      miniBatch = miniBatch,
      stepSize = stepSize,
      updater = updater,
      batchEvaluationSet = batchEvaluationSet,
      numPartitions = numPartitions,
      trainingFrequency = trainingFrequency)
  }

}
