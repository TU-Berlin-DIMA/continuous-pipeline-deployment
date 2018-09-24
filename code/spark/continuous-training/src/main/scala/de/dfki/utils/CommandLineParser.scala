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

  private def getOrElse[T](key: String, els: T): String = {
    map.getOrElse(key, els).toString
  }


  def get(key: String, els: String = ""): String = {
    map.getOrElse(key, els)
  }

  def getDouble(key: String, default: Double = 0.0): Double = {
    getOrElse(key, default).toDouble
  }

  def getLong(key: String, default: Long = 0L): Long = {
    getOrElse(key, default).toLong
  }

  def getInteger(key: String, default: Int = 0): Int = {
    getOrElse(key, default).toInt
  }

  def getBoolean(key: String, default: Boolean = false): Boolean = {
    getOrElse(key, default).toBoolean
  }

  def print(): Unit = {
    println(map)
  }

  private def parseArguments(argument: String): Unit = {
    val parts = argument.split("=", 2)
    map += (parts(0).trim -> parts(1).trim)
  }
}
