package de.dfki.deployment

import java.io.{File, FileWriter}

import de.dfki.core.scheduling.{FixedIntervalScheduler, FolderBasedScheduler}
import de.dfki.ml.optimization.{SquaredL2Updater, SquaredL2UpdaterWithMomentum}
import de.dfki.utils.CommandLineParser
import org.apache.spark.mllib.linalg.Vectors

/**
  * Training and Serving of SVM classifier using the architecture described in Velox: https://arxiv.org/abs/1409.3809
  * - Train a model on an initial dataset
  * - 'Deploy' the model and start relieving prediction queries and observations
  * - On prediction queries return the predicted label
  * - On training observations perform incremental update (to maximize throughput, data is arriving in mini batches)
  * - Retrain a model after a while, discard the current model and 'deploy' the new model
  *
  * TODO: This is a hackish solution ... Full implementation should trigger a complete restart of the spark context ..
  * Perform a batch retraining ... store the model and read the model in the streaming as the initial model
  *
  * @author Behrouz Derakhshan
  */
object VeloxClassifier extends Classifier {
  var slack: Long = _
  var tempRoot: String = _
  var incremental: Boolean = _

  /**
    * @param args arguments to the main class should be a set of key, value pairs in the format of key=value
    *             Velox Classifier:
    *             slack: delay between in periodic sgd iteration
    *             temp-path: path to write the observed data for retraining purposed
    *             refer to [[Classifier]] to view the rest of the arguments
    *
    */
  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def parseArgs(args: Array[String]) = {
    super.parseArgs(args)
    val parser = new CommandLineParser(args).parse()
    slack = parser.getLong("slack", defaultTrainingSlack)
    tempRoot = parser.get("temp-path", s"$BASE_DATA_DIRECTORY/temp-data")
    incremental = parser.getBoolean("incremental", default = true)
  }

  override def run(args: Array[String]): Unit = {
    parseArgs(args)
    var testType = ""
    if (testDataPath == "prequential") {
      testType = "prequential"
    } else {
      testType = "dataset"
    }
    val child = s"$getExperimentName/model-type-$modelType/num-iterations-$numIterations/" +
      s"slack-$slack/offline-step-$offlineStepSize/online-step-$onlineStepSize"

    val resultPath = experimentResultPath(resultRoot, child)
    val tempDirectory = experimentResultPath(tempRoot, child)
    if (modelPath == DEFAULT_MODEL_PATH) {
      modelPath = s"$resultRoot/$child/model"
    }
    createTempFolders(tempDirectory)
    val ssc = initializeSpark()

    // train initial model
    val startTime = System.currentTimeMillis()
    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + tempDirectory, modelType)
    val endTime = System.currentTimeMillis()
    storeTrainingTimes(endTime - startTime, resultPath)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    writeStreamToDisk(streamingSource.map(_._2.toString), tempDirectory)

    // evaluate the stream and incrementally update the model
    if (testDataPath == "prequential") {
      evaluateStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint), resultPath)
    } else {
      evaluateStream(testData, resultPath)
    }

    if (incremental) {
      trainOnStream(streamingSource.map(_._2.toString).map(dataParser.parsePoint))
    }

    // periodically retrain the model from scratch using the historical data
    val task = new Runnable {
      def run() {
        //println(streamingModel.latestModel().weights.toString)
        streamingSource.pause()

        storeRetrainingPoint(streamingSource.getLastProcessedFileIndex, resultPath)
        val startTime = System.currentTimeMillis()
        val before = streamingModel.latestModelWeights()
        val data = ssc.sparkContext.textFile(initialDataPath + "," + tempDirectory).map(dataParser.parsePoint)
        // retrain the initial model
        streamingModel
          .setConvergenceTol(1E-6)
          .setNumIterations(numIterations).trainInitialModel(data)
          .setUpdater(new SquaredL2UpdaterWithMomentum(0.9))
        //after retraining is done reset convergence tol and iteration count
        streamingModel
          .setConvergenceTol(0.0)
          .setNumIterations(1)
          .setStepSize(onlineStepSize)
          .setUpdater(new SquaredL2Updater)

        val after = streamingModel.latestModelWeights()
        val endTime = System.currentTimeMillis()
        storeTrainingTimes(endTime - startTime, resultPath)
        logger.info(s"Delta: ${Vectors.sqdist(before, after)}")
        streamingSource.unpause()
      }
    }

    // slack == -1 means a Folder based Scheduler
    val scheduler = if (slack == -1) {
      new FolderBasedScheduler(streamingSource, ssc, task)
    } else {
      new FixedIntervalScheduler(streamingSource, ssc, task, slack)
    }

    ssc.start()
    scheduler.init()
    scheduler.schedule()
    ssc.awaitTermination()

  }

  def storeRetrainingPoint(index: Int, resultPath: String) = {
    val file = new File(s"$resultPath/retraining-points.txt")
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file, true)
    fw.write(s"$index\n")
    fw.close()
  }

  override def getApplicationName = "Velox Classifer"

  override def getExperimentName = "velox"

  override def defaultBatchDuration = 1L

  override def defaultTrainingSlack = 60L

  override def defaultModelType = "svm"
}