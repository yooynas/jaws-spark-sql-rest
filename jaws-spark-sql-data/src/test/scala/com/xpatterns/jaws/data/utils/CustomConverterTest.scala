package com.xpatterns.jaws.data.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.apache.spark.sql.catalyst.types._
import org.apache.spark.sql.catalyst.expressions.Row
import com.xpatterns.jaws.data.DTO.Column
import com.xpatterns.jaws.data.DTO.CustomResult

@RunWith(classOf[JUnitRunner])
class CustomConverterTest extends FunSuite {
  val intField = new StructField("int", DataType("IntegerType"), false)
  val strField = new StructField("str", DataType("StringType"), true)
  val arrOfStrField = new StructField("arr", DataType("ArrayType(StringType,false)"), true)
  val arrOfStrFieldNotNullable = new StructField("arr", DataType("ArrayType(StringType,false)"), false)
  val arrOfRecField = new StructField("arrOfRec", DataType("ArrayType(StructType(List(StructField(str,StringType,true))),false)"), true)
  val mapOfStrField = new StructField("map", DataType("MapType(StringType,StringType,true)"), false)
  val mapOfRecField = new StructField("mapOfRec", DataType("MapType(StringType,StructType(List(StructField(str,StringType,true))),true)"), false)

  val structType = new StructType(Seq(intField, strField))
  val structTypeRow = Array(Row.fromSeq(Seq(1, "a")), Row.fromSeq(Seq(2, "b")))

  val recordType = new StructField("record", structType, false)
  val byteField = new StructField("byte", DataType("ByteType"), false)
  val binaryField = new StructField("binary", DataType("BinaryType"), true)
  val arrOfArrString = new StructField("arrOfArrayString", DataType("ArrayType(ArrayType(StringType,false),false)"), true)
  val mapOfMap = new StructField("mapOfMap", DataType("MapType(StringType, MapType(StringType,StringType,true),true)"), false)
  val arrOfMapofArr = new StructField("arrOfMapOfArr", DataType("ArrayType(MapType(StringType, ArrayType(ArrayType(StringType,false),false),true),false)"), true)
  val decimalField = new StructField("decimal", DataType("DecimalType"), true)

  test("simple schema") {
    val resultsConverter = new ResultsConverter(structType, structTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("int", "IntegerType", "", Array.empty),
      new Column("str", "StringType","", Array.empty))

    val expectedResult = Array(Array("1", "\"a\""), Array("2", "\"b\""))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")
  }

  test("results with record") {
    val complexStructType = new StructType(Seq(strField, recordType))
    val complexStructTypeRow = Array(Row.fromSeq(Seq("aa", Row.fromSeq(Seq(1, "a")))), Row.fromSeq(Seq("bb", Row.fromSeq(Seq(2, "b")))))

    val resultsConverter = new ResultsConverter(complexStructType, complexStructTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("str", "StringType","", Array.empty),
      new Column("record", "StructType","", Array(
        new Column("int", "IntegerType","", Array.empty),
        new Column("str", "StringType", "",Array.empty))))

    val expectedResult = Array(Array("\"aa\"", "[1,\"a\"]"), Array("\"bb\"", "[2,\"b\"]"))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")

  }

  test("results with list of strings") {
    val arrStructType = new StructType(Seq(intField, arrOfStrField))
    val arrStructTypeRow = Array(Row.fromSeq(Seq(1, Row.fromSeq(Seq("a", "b", "c")))), Row.fromSeq(Seq(2, Row.fromSeq(Seq("d", "e", "f")))))

    val resultsConverter = new ResultsConverter(arrStructType, arrStructTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("int", "IntegerType","", Array.empty),
      new Column("arr", "ArrayType", "",Array(
        new Column("items", "StringType","", Array.empty))))

    val expectedResult = Array(Array("1", "[\"a\",\"b\",\"c\"]"), Array("2", "[\"d\",\"e\",\"f\"]"))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")
  }

  test("results with list of records") {
    val arrStructType = new StructType(Seq(intField, arrOfRecField))
    val arrStructTypeRow = Array(
      Row.fromSeq(
        Seq(1, Row.fromSeq(
          Seq(Row.fromSeq(
            Seq("a")),
            Row.fromSeq(
              Seq("b")))))),
      Row.fromSeq(
        Seq(2, Row.fromSeq(
          Seq(Row.fromSeq(
            Seq("c")), Row.fromSeq(
            Seq("d")))))))

    val resultsConverter = new ResultsConverter(arrStructType, arrStructTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("int", "IntegerType", "", Array.empty),
      new Column("arrOfRec", "ArrayType","", Array(
        new Column("items", "StructType", "", Array(new Column("str", "StringType", "",Array.empty))))))

    val expectedResult = Array(Array("1", "[[\"a\"],[\"b\"]]"), Array("2", "[[\"c\"],[\"d\"]]"))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")

  }

  test("result with map of strings") {
    val mapStructType = new StructType(Seq(intField, mapOfStrField))

    val mapStructTypeRow = Array(Row.fromSeq(
      Seq(1, Map("a" -> "b", "c" -> "d"))),
      Row.fromSeq(
        Seq(2, Map("d" -> "e", "f" -> "g"))))

    val resultsConverter = new ResultsConverter(mapStructType, mapStructTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("int", "IntegerType", "", Array.empty),
      new Column("map", "MapType", "", Array(
        new Column("values", "StringType", "", Array.empty))))

    val expectedResult = Array(Array("1", "{\"a\":\"b\",\"c\":\"d\"}"), Array("2", "{\"d\":\"e\",\"f\":\"g\"}"))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")
  }

  test("results with map of records") {
    val mapStructType = new StructType(Seq(intField, mapOfRecField))
    val mapStructTypeRow = Array(Row.fromSeq(
      Seq(1, Map("a" -> Row.fromSeq(Seq("b")), "c" -> Row.fromSeq(Seq("d"))))),
      Row.fromSeq(
        Seq(2, Map("d" -> Row.fromSeq(Seq("e")), "f" -> Row.fromSeq(Seq("g"))))))

    val resultsConverter = new ResultsConverter(mapStructType, mapStructTypeRow)
    val customRes = resultsConverter.toCustomResults()

    val expectedSchema = Array(new Column("int", "IntegerType", "", Array.empty),
      new Column("mapOfRec", "MapType", "", Array(
        new Column("values", "StructType", "", Array(new Column("str", "StringType", "", Array.empty))))))

    val expectedResult = Array(Array("1", "{\"a\":[\"b\"],\"c\":[\"d\"]}"), Array("2", "{\"d\":[\"e\"],\"f\":[\"g\"]}"))

    val expectedCustomResult = new CustomResult(expectedSchema, expectedResult)
    assert(customRes === expectedCustomResult, "Different custom results")
  }
  
}


