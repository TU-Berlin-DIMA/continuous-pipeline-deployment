package de.dfki.core.scheduling

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture}

import de.dfki.utils.BatchFileInputDStream
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.log4j.Logger
import org.apache.spark.streaming.StreamingContext

/**
  * @author behrouz
  */
abstract class Scheduler(streamingSource: BatchFileInputDStream[LongWritable, Text, TextInputFormat],
                         ssc: StreamingContext,
                         task: Runnable) {

  @transient protected var future: ScheduledFuture[_] = _
  @transient protected var execService: ScheduledExecutorService = _
  @transient protected val logger = Logger.getLogger(getClass.getName)

  def init(): Unit = {
    execService = Executors.newSingleThreadScheduledExecutor()
  }

  def runNow(): Unit

  def schedule(): Unit

  def schedulingType(): String

  def stop(): Unit = {
    logger.info("Shutting down the scheduler")
    future.cancel(true)
    execService.shutdown()
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }


}
