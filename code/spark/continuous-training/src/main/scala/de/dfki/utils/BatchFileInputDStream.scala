package de.dfki.utils


import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, PathFilter}
import org.apache.hadoop.mapreduce.{InputFormat => NewInputFormat}
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.reflect.ClassTag

/**
  * Created by bede01 on 24/11/16.
  */
class BatchFileInputDStream[K, V, F <: NewInputFormat[K, V]](
                                                              _ssc: StreamingContext,
                                                              directory: String,
                                                              filter: Path => Boolean = BatchFileInputDStream.defaultFilter,
                                                              conf: Option[Configuration] = None)
                                                            (implicit km: ClassTag[K], vm: ClassTag[V], fm: ClassTag[F])
  extends InputDStream[(K, V)](_ssc) with Serializable {
  @transient private val logger = Logger.getLogger(getClass.getName)


  @transient private var _path: Path = _
  @transient private var _fs: FileSystem = _
  @transient private var _files: Array[String] = _
  @transient private var lastProcessedFileIndex = 0
  @transient private var isPaused = false


  private def directoryPath: Path = {
    if (_path == null) _path = new Path(directory)
    _path
  }

  private def fs: FileSystem = {
    if (_fs == null) _fs = directoryPath.getFileSystem(context.sparkContext.hadoopConfiguration)
    _fs
  }

  private def files: Array[String] = {
    if (_files == null) _files = listFiles()
    _files
  }

  private def reset() {
    _fs = null
  }


  override def start() {
    lastProcessedFileIndex = 0

  }

  override def stop(): Unit = {
  }

  def rddFromFile(s: String): RDD[(K, V)] = {
    context.sparkContext.newAPIHadoopFile[K, V, F](s)
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
    else if (lastProcessedFileIndex < files.length) {
      val rdd = rddFromFile(files(lastProcessedFileIndex))
      lastProcessedFileIndex += 1
      Option(rdd)
    } else {
      logger.warn("All Files in the directory are processed!!!")
      None
    }
  }

  def isCompleted: Boolean = {
    lastProcessedFileIndex >= files.length
  }


  def pause(): Unit = {
    logger.info("Pause streaming source")
    isPaused = true
  }

  def unpause(): Unit = {
    logger.info("Resume streaming source")
    isPaused = false
  }

  private def deleteFile(s: String): Unit = {
    fs.delete(new Path(s), true)
  }

  def sortByFolderName(p1: Path, p2: Path) = {
    p1.getName > p2.getName
  }

  def listFiles(): Array[String] = {

    val directoryFilter = new PathFilter {
      override def accept(path: Path): Boolean = fs.getFileStatus(path).isDirectory
    }

    val pathFilter = new PathFilter {
      override def accept(path: Path): Boolean = filter(path)
    }
    val directories = fs.globStatus(directoryPath, directoryFilter).map(_.getPath)

    val allFiles = directories.sortWith(sortByFolderName).flatMap(dir =>
      fs.listStatus(dir, pathFilter).map(_.getPath.toString))
    allFiles
  }
}


object BatchFileInputDStream {

  // skip files starting with . and _ (for success)
  def defaultFilter(path: Path): Boolean = !path.getName.startsWith(".") && !path.getName.startsWith("_")
}
