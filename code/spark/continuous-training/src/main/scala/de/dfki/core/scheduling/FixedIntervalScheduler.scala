package de.dfki.core.scheduling

import java.util.concurrent._

import de.dfki.core.streaming.BatchFileInputDStream
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.streaming.StreamingContext

/**
  * @author bede01.
  */
class FixedIntervalScheduler(streamingSource: BatchFileInputDStream[LongWritable, Text, TextInputFormat],
                             ssc: StreamingContext,
                             task: Runnable,
                             interval: Long
                            ) extends Scheduler(streamingSource, ssc, task) {

  def schedule(): Unit = {
    if (streamingSource.isCompleted | ssc.sparkContext.isStopped) {
      logger.warn("Streaming source is depleted")
      stop()
    } else {
      logger.warn(s"Scheduling new training to run in $interval seconds")
      future = execService.schedule(task, interval, TimeUnit.SECONDS)
      future.get()
      while (!future.isDone) {
        Thread.sleep(5000)
      }
      schedule()

    }
  }

  def runNow(): Unit = {
    execService.execute(task)
  }

  override def schedulingType = FixedIntervalScheduler.SCHEDULING_TYPE

}

object FixedIntervalScheduler {
  val SCHEDULING_TYPE = "fixed-interval-scheduling"
}
