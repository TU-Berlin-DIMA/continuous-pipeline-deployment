package de.dfki.classification

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.utils.MLUtils._
import org.apache.log4j.Logger

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
object VeloxClassifier extends SVMClassifier {

  val slack = 100l
  var now = 0l
  @transient private val logger = Logger.getLogger(getClass.getName)

  /**
    *
    * @param args
    * prequential/static
    * initial-training-path
    * streaming-path
    * test-path(if static)
    */
  def main(args: Array[String]): Unit = {
    run(args)
  }

  override def getApplicationName: String = "Velox SVM Model"

  override def run(args: Array[String]): Unit = {
    createTempFolders(historicalData)
    val (evaluationType, initialDataPath, streamingDataPath, testDataPath) = parseArgs(args, BASE_DATA_DIRECTORY)

    val ssc = initializeSpark()


    streamingModel = createInitialStreamingModel(ssc, initialDataPath + "," + historicalData)
    val streamingSource = streamSource(ssc, streamingDataPath)
    val testData = constantInputDStreaming(ssc, testDataPath)

    if (evaluationType == EvaluationType.Prequential) {
      prequentialStreamEvaluation(streamingSource.map(_._2.toString).map(parsePoint), "results/velox")
    } else {
      streamProcessing(testData, streamingSource.map(_._2.toString).map(parsePoint), "results/velox")
    }


    val task = new Runnable {
      def run() {
        logger.info("initiating a retraining of the model ...")
        streamingSource.pause()
        val model = trainModel(ssc.sparkContext, initialDataPath + "," + historicalData, 500)
        streamingModel.setInitialModel(model)
        logger.info("Model was re-trained ...")
        if (streamingSource.isCompleted()) {
          logger.warn("stopping the program")
          ssc.stop(stopSparkContext = true, stopGracefully = true)
          future.cancel(true)
          execService.shutdown()
        }
        streamingSource.unpause()
      }
    }

    ssc.start()

    execService = Executors.newSingleThreadScheduledExecutor()

    future = execService.scheduleAtFixedRate(task, slack, slack, TimeUnit.SECONDS)
    future.get()

  }
}