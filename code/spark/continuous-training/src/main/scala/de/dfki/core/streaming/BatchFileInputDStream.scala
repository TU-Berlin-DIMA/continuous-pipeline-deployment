package de.dfki.core.streaming

import de.dfki.core.scheduling.{FixedIntervalScheduler, FolderBasedScheduler}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, PathFilter}
import org.apache.hadoop.mapreduce.{InputFormat => NewInputFormat}
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.reflect.ClassTag

/**
  * @author Behrouz
  */
class BatchFileInputDStream[K, V, F <: NewInputFormat[K, V]](_ssc: StreamingContext,
                                                             directory: String,
                                                             filter: Path => Boolean = BatchFileInputDStream.defaultFilter,
                                                             conf: Option[Configuration] = None,
                                                             days: Array[Int] = Array(1, 2, 3, 4, 5))
                                                            (implicit km: ClassTag[K], vm: ClassTag[V], fm: ClassTag[F])
  extends InputDStream[(K, V)](_ssc) with Serializable {

  @transient private val logger = Logger.getLogger(getClass.getName)


  @transient private var _path: Path = _
  @transient private var _fs: FileSystem = _
  @transient private var _files: Array[String] = _

  @transient private var processedFiles: Array[String] = Array()
  @transient private var lastProcessedFileIndex = 0
  @transient private var isPaused = false
  @transient private var schedulingPolicy = FixedIntervalScheduler.SCHEDULING_TYPE


  def files: Array[String] = {
    if (_files == null) _files = listFiles()
    _files
  }


  def setLastIndex(index: Int): Unit = {
    this.lastProcessedFileIndex = index
  }

  def getLastIndex = this.lastProcessedFileIndex

  /**
    * starting the streaming source
    * called by spark
    */
  override def start() {
    lastProcessedFileIndex = 0

  }

  /**
    * stopping the source
    * called by spark
    */
  override def stop(): Unit = {
  }

  /**
    * set scheduling policy
    *
    * @param policy the scheduling policy (FolderBased on Interval)
    */
  def setSchedulingPolicy(policy: String) = {
    schedulingPolicy = policy
  }

  /**
    * checks whether all the files are processed or not
    *
    * @return
    */
  def isCompleted: Boolean = lastProcessedFileIndex >= files.length

  /**
    * returns the list of processed files, used for retraining
    *
    * @return list of processing files
    */
  def getProcessedFiles = processedFiles

  /**
    * returns list of all files processed or queued for processing
    *
    * @return list of all files
    */
  def getAllFiles = files

  /**
    * returns the current folder that the files are being processed from
    *
    * @return current folder being processed
    */
  def getCurrentFolder = {
    if (!isPattern) {
      BatchFileInputDStream.FOLDER_IS_FLAT
    }
    else if (lastProcessedFileIndex >= files.length) {
      BatchFileInputDStream.NO_MORE_FOLDERS
    }
    else {
      val s = files(lastProcessedFileIndex)
      val i2 = s.lastIndexOf("/")
      val i1 = s.lastIndexOf("/", i2 - 1)
      s.substring(i1 + 1, i2)
    }
  }

  /**
    * returns the next folder that the files will be processed from
    *
    * @return next folder for processing
    */
  def getNextFolder = {
    if (!isPattern) {
      BatchFileInputDStream.FOLDER_IS_FLAT
    } else if (lastProcessedFileIndex >= files.length - 1) {
      BatchFileInputDStream.NO_MORE_FOLDERS
    } else {
      val s = files(lastProcessedFileIndex + 1)
      val i2 = s.lastIndexOf("/")
      val i1 = s.lastIndexOf("/", i2 - 1)
      s.substring(i1 + 1, i2)
    }
  }

  /**
    * pause streaming source
    */
  def pause() {
    logger.info("Pause streaming source")
    isPaused = true
  }

  /**
    * resume streaming source
    */
  def resume() {
    logger.info("Resume streaming source")
    isPaused = false
  }


  /**
    * Each call to compute returns an RDD created from a file in the given directory.
    * validTime input parameter is ignored
    *
    * @param validTime ignored
    * @return RDD created from the a file in the given directory
    */
  override def compute(validTime: Time): Option[RDD[(K, V)]] = {
    if (isPaused) {
      logger.warn("This streaming source is paused!!!")
      None
    }
    else if (!allFileProcessed()) {
      val rdd = rddFromFile(files(lastProcessedFileIndex))
      if (getCurrentFolder != getNextFolder &
        getNextFolder != BatchFileInputDStream.NO_MORE_FOLDERS &
        schedulingPolicy == FolderBasedScheduler.SCHEDULING_TYPE) {
        // this is not the ideal behaviour, as streaming and
        // batch learning can and should happen simultaneously
        logger.info(s"Finished Processing Folder: $getCurrentFolder")
        logger.warn(s"Streaming source is paused until new training is scheduled")
        //pause()
      }
      processedFiles = processedFiles :+ files(lastProcessedFileIndex)
      lastProcessedFileIndex += 1
      Option(rdd)
    } else {
      logger.warn("All Files in the directory are processed!!!")
      None
    }
  }

  /**
    * same as compute
    * Used for manual simulation
    *
    * @return RDD created from the a file in the given directory
    */
  def generateNextRDD(): Option[RDD[(K, V)]] = {
    if (!allFileProcessed()) {
      logger.info(s"reading ${files(lastProcessedFileIndex)}")
      val rdd = rddFromFile(files(lastProcessedFileIndex))
      processedFiles = processedFiles :+ files(lastProcessedFileIndex)
      lastProcessedFileIndex += 1
      Option(rdd)
    } else {
      logger.warn("All Files in the directory are processed!!!")
      None
    }
  }

  def allFileProcessed(): Boolean = {
    lastProcessedFileIndex >= files.length
  }

  private def listFiles(): Array[String] = {
    val directoryPath = new Path(directory)
    val fs = directoryPath.getFileSystem(context.sparkContext.hadoopConfiguration)
    val directoryFilter = new PathFilter {
      override def accept(path: Path): Boolean = fs.getFileStatus(path).isDirectory
    }

    val pathFilter = new PathFilter {
      override def accept(path: Path): Boolean = filter(path)
    }
    val directories = days.flatMap { d =>
      fs.globStatus(new Path(s"$directory/day_$d"), directoryFilter).map(_.getPath)
    }

    val allFiles = directories
      .flatMap(dir => fs.listStatus(dir, pathFilter).map(_.getPath.toString).sortWith(sortByName))

    allFiles
  }

  private def sortByName(s1: String, s2: String) = {
    s1 < s2
  }

  private def isPattern = {
    directory contains "*"
  }

  private def rddFromFile(s: String): RDD[(K, V)] = {
    context.sparkContext.newAPIHadoopFile[K, V, F](s)
  }

}


object BatchFileInputDStream {
  val FOLDER_IS_FLAT = "folder is flat"
  val NO_MORE_FOLDERS = "no more folders"

  // skip files starting with . and _ (for success)
  def defaultFilter(path: Path): Boolean = !path.getName.startsWith(".") && !path.getName.startsWith("_")
}
