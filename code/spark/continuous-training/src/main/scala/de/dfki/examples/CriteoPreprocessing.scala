package de.dfki.examples

import java.io.IOException

import de.dfki.utils.CommandLineParser
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.types.{DoubleType, StringType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, sql}


/**
  * Preprocess the Crieto dataset by using one hot encoder on all the categorical features
  * We have to use a hacky solution since the samples from each day may not have the same set of features.
  * Therefore, the entire dataset has to be processed together
  *
  * @author bede01.
  *
  */
object CriteoPreprocessing {
  val INPUT_PATH = "data/criteo-full"
  val OUTPUT_PATH = "data/criteo-full/processed"
  val FILES_PER_DAY = 100
  // There are a total of 23 days
  val DAYS = "0,1,2,3,4,5,6"

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val root = parser.get("input-path", INPUT_PATH)
    val resultPath = parser.get("output-path", OUTPUT_PATH)
    val days = parser.get("days", DAYS).split(",").map(_.trim).map(_.toInt)
    val filesPerDay = parser.getInteger("files-per-day", FILES_PER_DAY)

    val conf = new SparkConf().setAppName("Criteo Feature Engineering")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    var indexerModel: PipelineModel = null
    try {
      // load the model if it already exists
      indexerModel = PipelineModel.load(s"$root/model/")
    }
    catch {
      case iie: IOException =>
        indexerModel = createIndexerModel(spark, root)
    }

    // use the indexer model to transform the data
    // this way we are ensuring that the one hot encoding is unified accross all days
    for (d <- days) {
      val dataFrame = spark.read
        .format("csv")
        .option("delimiter", ",")
        .option("header", "false")
        .option("inferSchema", "true")
        .load(s"$root/raw/$d")

      // add missing values NULL for string and 0.0 for double
      var columnMapping = collection.mutable.Map[String, Any]()
      for (t <- dataFrame.schema.fields) {
        if (t.dataType == StringType) {
          columnMapping += (t.name -> "NULL")
        } else if (t.dataType == DoubleType) {
          columnMapping += (t.name -> 0.0)
        }
      }
      val map = Map() ++ columnMapping
      val filledDf = dataFrame.na.fill(map)

      val indexedDf = indexerModel.transform(filledDf)

      val indexColumns = indexedDf.columns.filter(x => x contains "index")

      val oneHotEncoder: Array[org.apache.spark.ml.PipelineStage] = indexColumns.map(
        cname => new OneHotEncoder()
          .setInputCol(cname)
          .setOutputCol(s"${cname}_vec")
      )
      val oneHotPipeline = new Pipeline().setStages(oneHotEncoder)
      val finalDf = oneHotPipeline.fit(indexedDf).transform(indexedDf)

      dataFrame.unpersist(true)
      // assemble the preprocessed columns into vectors
      val finalColumns = (1 to 13).map(v => s"_c$v") ++ finalDf.columns.filter(c => c contains "vec")
      val assembler = new VectorAssembler()
        .setInputCols(finalColumns.toArray)
        .setOutputCol("features")

      val output = assembler.transform(finalDf).select("_c0", "features").repartition(filesPerDay)
      // without explicitly caching the dataset, the program runs out of memory on my local machine
      output.cache()
      output.count()


      finalDf.unpersist(true)
      dataFrame.unpersist(true)
      output.rdd.map(r => s"${r.get(0).toString} | ${r.get(1).toString}").saveAsTextFile(s"$resultPath/$d/")
      output.unpersist(true)
    }

  }

  def createIndexerModel(spark: SparkSession, root: String): PipelineModel = {
    val df = spark.read
      .format("csv")
      .option("delimiter", ",")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(s"$root/raw/backup")


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

    // train an indexer on the entire data
    // then use it to index and encode individual datasets
    val indexerPipeline = new Pipeline().setStages(indexTransformers.toArray)
    val indexerModel = indexerPipeline.fit(filledDf)
    filledDf.unpersist(true)
    df.unpersist(true)
    indexerModel.save(s"$root/model/")
    indexerModel
  }

}


// On the local version we are working with a sample and as a result not all of the
// days will contains the same set of categorical variables
// In order to do one hot encoding of the data, we have to combine all the data, make the necessary
// transformations and then separate them
object AddDayColumn {
  val INPUT_PATH = "data/criteo-full/raw"
  val OUTPUT_PATH = "data/criteo-full/all"

  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args).parse()
    val root = parser.get("input-path", INPUT_PATH)
    val result = parser.get("output-path", OUTPUT_PATH)

    val days = 0 to 4

    val conf = new SparkConf().setAppName("Criteo Feature Engineering")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.set("spark.executor.memory", "5g")
    conf.setMaster(masterURL)

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    for (d <- days) {
      val day = processDay(spark, root, d)
      day.write.option("header", "false").mode(SaveMode.Append).csv(path = result)
    }

  }

  def processDay(spark: SparkSession, root: String, day: Int): sql.DataFrame = {
    val df = spark.read
      .format("csv")
      .option("delimiter", ",")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(s"$root/$day/")
    df.withColumn("day", lit(day))
  }
}

object CriteoPreprocessingOneDay {
  val INPUT_PATH = "data/criteo-full"
  val OUTPUT_PATH = "data/criteo-full/processed"
  val INDEXER_MODEL_PATH = "data/criteo-full/model"
  val FILES_PER_DAY = 100

  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val inputData = parser.get("input-path", INPUT_PATH)
    val resultPath = parser.get("output-path", OUTPUT_PATH)
    val filesPerDay = parser.getInteger("files-per-day", FILES_PER_DAY)
    val modelPath = parser.get("model-path", INDEXER_MODEL_PATH)

    val conf = new SparkConf().setAppName("Criteo Feature Engineering")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    val dataFrame = spark.read
      .format("csv")
      .option("delimiter", "\t")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(s"$inputData")

    var indexerModel: PipelineModel = null
    try {
      // load the model if it already exists
      indexerModel = PipelineModel.load(s"$modelPath")
    }
    catch {
      case iie: IOException =>
        indexerModel = createIndexerModel(spark, dataFrame, modelPath)
    }
    // add missing values NULL for string and 0.0 for double
    var columnMapping = collection.mutable.Map[String, Any]()
    for (t <- dataFrame.schema.fields) {
      if (t.dataType == StringType) {
        columnMapping += (t.name -> "NULL")
      } else if (t.dataType == DoubleType) {
        columnMapping += (t.name -> 0.0)
      }
    }
    val map = Map() ++ columnMapping
    val filledDf = dataFrame.na.fill(map)

    val indexedDf = indexerModel.transform(filledDf)

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

    val output = assembler.transform(finalDf).select("_c0", "features").repartition(filesPerDay)
    // without explicitly caching the dataset, the program runs out of memory on my local machine

    output.rdd.map(r => s"${r.get(0).toString} | ${r.get(1).toString}").saveAsTextFile(s"$resultPath/")
    dataFrame.unpersist()
  }

  def createIndexerModel(spark: SparkSession, df: DataFrame, modelPath: String): PipelineModel = {
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

    // train an indexer on the entire data
    // then use it to index and encode individual datasets
    val indexerPipeline = new Pipeline().setStages(indexTransformers.toArray)
    val indexerModel = indexerPipeline.fit(filledDf)
    filledDf.unpersist(true)
    df.unpersist(true)
    indexerModel.save(s"$modelPath")
    indexerModel
  }
}

