package de.dfki.utils

/**
  * Simple utility class for parsing command line arguments
  * passed as key=value pair
  */
class CommandLineParser(val args: Array[String]) {
  private val map = collection.mutable.Map[String, String]()

  def parse(): this.type = {
    args.foreach(parseArguments)
    this
  }

  def getOrElse[T](key: String, els: T): T = {
    map.getOrElse(key, els).asInstanceOf[T]
  }


  def get(key: String): String = {
    map(key)
  }

  def getDouble(key: String, default: Double = 0.0): Double = {
    getOrElse(key, default)
  }

  def getLong(key: String, default: Long = 0L): Long = {
    getOrElse(key, default)
  }

  def getInteger(key: String, default: Int = 0): Int = {
    getOrElse(key, default)
  }

  def print(): Unit = {
    println(map)
  }

  private def parseArguments(argument: String): Unit = {
    val parts = argument.split("=")
    map += (parts(0).trim -> parts(1).trim)
  }
}
