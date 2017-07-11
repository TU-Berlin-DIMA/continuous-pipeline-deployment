package de.dfki.preprocessing.parsers

import org.scalatest.FunSuite

/**
  * @author bede01.
  */
class CustomVectorParserTest extends FunSuite {

  test("testUnparsePoint") {
    val point = "0 | (1217776,[0,1,2,7,8,11,12,13992,236326,254153,265150,271856,289787,291050,296240,297437,297760,480839,529120,576035,576042,578096,584474,584531,584534,585437,599057,839653,955794,1169838,1210252,1217683,1217746],[6.0,324.0,5.0,1.0,5.0,723432.0,5.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])"

    val vec = new CustomVectorParser().parsePoint(point)
    print(vec.toString())
  }

  test("testParsePoint") {

  }

}
