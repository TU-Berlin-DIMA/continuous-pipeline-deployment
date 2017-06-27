package de.dfki.examples

import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, StringType}

/**
  * Preprocess the Crieto dataset by using one hot encoder on all the categorical features
  * We have to use a hacky solution since the samples from each day may not have the same set of features.
  * Therefore, the entire dataset has to be processed together
  *
  * @author bede01.
  *
  */
object CriteoPreprocessing {
  val INPUT_PATH = "data/criteo-full/"
  val OUTPUT_PATH = "data/criteo-full/processed/"

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val data = parser.get("input-path", INPUT_PATH)
    val result = parser.get("output-path", OUTPUT_PATH)

    val conf = new SparkConf().setAppName("Criteo Feature Engineering")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    val df = spark.read
      .format("csv")
      .option("delimiter", ",")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(data)


    // add missing values NULL for string and 0.0 for double
    var columnMapping = collection.mutable.Map[String, Any]()
    for (t <- df.schema.fields) {
      if (t.dataType == StringType) {
        columnMapping += (t.name -> "NULL")
      } else if (t.dataType == DoubleType) {
        columnMapping += (t.name -> 0.0)
      }
    }
    val map = Map() ++ columnMapping
    val filledDf = df.na.fill(map)


    // add index for all the categorical columns
    val stringColumns = (14 to 39).map(d => s"_c$d")
    val indexTransformers = stringColumns.map {
      colName =>
        new StringIndexer()
          .setInputCol(colName)
          .setOutputCol(s"${colName}_index")
    }

    val indexerPipeline = new Pipeline().setStages(indexTransformers.toArray)
    val indexedDf = indexerPipeline.fit(filledDf).transform(filledDf)


    // transform categorical columns using the one hot encoder
    val indexColumns = indexedDf.columns.filter(x => x contains "index")
    val oneHotEncoder: Array[org.apache.spark.ml.PipelineStage] = indexColumns.map(
      cname => new OneHotEncoder()
        .setInputCol(cname)
        .setOutputCol(s"${cname}_vec")
    )
    val oneHotPipeline = new Pipeline().setStages(oneHotEncoder)
    val finalDf = oneHotPipeline.fit(indexedDf).transform(indexedDf)


    // assemble the preprocessed columns into vectors
    val finalColumns = (1 to 13).map(v => s"_c$v") ++ finalDf.columns.filter(c => c contains "vec")
    val assembler = new VectorAssembler()
      .setInputCols(finalColumns.toArray)
      .setOutputCol("features")

    val output = assembler.transform(finalDf)
    output.createOrReplaceTempView("result")

    // store the preprocessed data
    // _c40 represent the day number, it was added to the source dataset inorder to have consistent one hot encoding
    // scheme
    val output0 = spark.sql("select _c0, features from result where _c40 = 0")
    output0.rdd.map(r => (r.get(0), r.get(1))).saveAsTextFile(s"$result/0/")

    val output1 = spark.sql("select _c0, features from result where _c40 = 1")
    output1.rdd.map(r => (r.get(0), r.get(1))).saveAsTextFile(s"$result/1/")

  }

}

