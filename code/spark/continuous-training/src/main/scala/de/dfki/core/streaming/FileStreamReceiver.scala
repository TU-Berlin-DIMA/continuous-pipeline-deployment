package de.dfki.core.streaming

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, PathFilter}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

import scala.io.Source._


/**
  * Created by bede01 on 03/01/17.
  */
class FileStreamReceiver(directory: String) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) {

  @transient private var _path: Path = null
  @transient private var _fs: FileSystem = null
  @transient private var _lastProcessedFileIndex = 0

  private def directoryPath: Path = {
    if (_path == null) _path = new Path(directory)
    _path
  }

  private def fs: FileSystem = {
    if (_fs == null) _fs = FileSystem.get(new Configuration())
    _fs
  }

  override def onStart(): Unit = {
    // Start the thread that receives data over a connection
    new Thread("File Receiver") {
      override def run() {
        receive()
      }
    }.start()
  }

  override def onStop(): Unit = {
    _lastProcessedFileIndex = 0
    // do nothing
  }


  def receive(): Unit = {
    val files = listFiles()
    while (!isStopped) {
      if (_lastProcessedFileIndex < files.length) {
        // read the file in its entirety and call store
        val path = new Path(files(_lastProcessedFileIndex))
        val br = fromInputStream(fs.open(path));
        br.getLines()
        // this is blocking
        store(br.getLines())
        // delete the file once it was read
        fs.delete(path, true)
      }
      _lastProcessedFileIndex += 1
    }
  }


  private def listFiles(): Array[String] = {

    val directoryFilter = new PathFilter {
      override def accept(path: Path): Boolean = fs.getFileStatus(path).isDirectory
    }

    val pathFilter = new PathFilter {
      override def accept(path: Path): Boolean = defaultFilter(path)
    }
    val directories = fs.globStatus(directoryPath, directoryFilter).map(_.getPath)

    val allFiles = directories.flatMap(dir =>
      fs.listStatus(dir, pathFilter).map(_.getPath.toString))
    allFiles
  }

  def defaultFilter(path: Path): Boolean = !path.getName().startsWith(".") && !path.getName().startsWith("_")
}
