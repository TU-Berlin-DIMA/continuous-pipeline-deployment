package de.dfki.utils

import org.scalatest.FunSuite

/**
  * Created by bede01 on 23/01/17.
  */
class CommandLineParserTest extends FunSuite {

  test("testGet") {
    val c = new CommandLineParser(Array("key1=value1", "key2=10.0", "key3=11", "key4=12")).parse()
    assert(c.get("key1") == "value1")
    assert(c.getDouble("key2") == 10.0)
    assert(c.getLong("key3") == 11L)
    assert(c.getInteger("key4") == 12)

  }
}
