package de.dfki.examples

import java.util.concurrent.{Executors, TimeUnit}

import de.dfki.streaming.models.HybridSVM
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.DenseVector

import scala.util.Random

/**
  * Created by bede01 on 12/01/17.
  */
object TestStateSharingOverThreads {
  val execService = Executors.newSingleThreadScheduledExecutor()

  def doStuff(streamingModel: HybridSVM) = {

    val task2 = new Runnable {
      override def run() = {
        println(streamingModel.toString)
      }
    }
    val future2 = execService.scheduleAtFixedRate(task2, 2, 5, TimeUnit.SECONDS)
  }

  def main(args: Array[String]): Unit = {

    val streamingModel = new HybridSVM().setInitialModel(new SVMModel(new DenseVector(List(0.0, 0, 0).toArray), 0.0))
    doStuff(streamingModel)

    val task = new Runnable {
      def run() = {
        streamingModel.setInitialModel(new SVMModel(new DenseVector(List(0.0, 0, 0).toArray), Random.nextDouble()))
      }
    }


    //ssc.awaitTermination()
    val future = execService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS)

  }

}
