package de.dfki.core.scheduling

import java.util.concurrent.TimeUnit

import de.dfki.utils.BatchFileInputDStream
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.streaming.StreamingContext

/**
  * This scheduler class is specifically designed for Velox training.
  * It assumes the data is stored in a folder hierarchy:
  *
  * root/
  * folder1/
  * folder2/
  * folder3/
  * ...
  *
  * It triggers one retraining after each folder is processed
  *
  * @author bede01.
  */
class FolderBasedScheduler(streamingSource: BatchFileInputDStream[LongWritable, Text, TextInputFormat],
                           ssc: StreamingContext,
                           task: Runnable) extends Scheduler(streamingSource, ssc, task) {

  var currentFolder = "-1"
  override def init() = {
    super.init()
    currentFolder = streamingSource.currentFolder

  }
  override def runNow() = ???

  override def schedule() = {
    var nextFolder = streamingSource.nextFolder
    if (streamingSource.isCompleted | ssc.sparkContext.isStopped) {
      logger.warn("Streaming source is depleted")
      stop()
    }
    while (nextFolder == currentFolder) {
      Thread.sleep(5000)
      nextFolder = streamingSource.nextFolder
    }
    currentFolder = streamingSource.currentFolder
    logger.info("Executing a new batch retraining")
    future = execService.schedule(task, 0, TimeUnit.SECONDS)
    future.get()
    while (!future.isDone) {
      Thread.sleep(5000)
    }
    schedule()
  }


}
