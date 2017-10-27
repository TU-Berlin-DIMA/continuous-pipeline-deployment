package de.dfki.deployment

import de.dfki.ml.pipelines.Pipeline
import de.dfki.preprocessing.parsers.CustomVectorParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable.ListBuffer

/**
  * @author behrouz
  */
class PeriodicalDeploymentWithMaterialization(val history: String,
                                              val stream: String,
                                              val eval: String,
                                              val materializedLocation: String,
                                              val resultPath: String) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = (1 to 5).map(i => s"$stream/day_$i")
    var copyPipeline = pipeline
    copyPipeline.setMaterialization(true)


    val testData = streamingContext.sparkContext.textFile(eval)

    val spark = streamingContext.sparkContext

    val dataParser = new CustomVectorParser()
    val historyData = spark.textFile(history)
    val materializedHistory = copyPipeline.update(historyData)
    materializedHistory.map(dataParser.unparsePoint).saveAsTextFile(s"$materializedLocation/0")
    var trainingDays: ListBuffer[String] = new ListBuffer[String]()
    trainingDays += s"$materializedLocation/0"
    var i = 1
    for (day <- days) {
      copyPipeline.setMaterialization(true)
      val data = streamingContext.sparkContext
        .textFile(day)

      val materialized = copyPipeline.update(data)
      materialized.map(dataParser.unparsePoint).saveAsTextFile(s"$materializedLocation/$i")
      i += 1
      trainingDays += s"$materializedLocation/$i"
    }

    // initial evaluation
    evaluateStream(copyPipeline, testData, resultPath)
    for (day <- trainingDays) {
      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.setMaterialization(true)
      trainingDays += day
      val data = streamingContext.sparkContext
        .textFile(trainingDays.mkString(","))
      val startTime = System.currentTimeMillis()
      copyPipeline.update(data)
      copyPipeline.train(data)
      val endTime = System.currentTimeMillis()

      storeTrainingTimes(endTime - startTime, resultPath)

      evaluateStream(copyPipeline, testData, resultPath)
    }
  }

  def writeStreamToDisk(rdd: RDD[String], path: String, index: Int): Unit = {
    val hadoopConf = new Configuration()
    hadoopConf.set("mapreduce.output.basename", s"$index-")
    rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$path", classOf[NullWritable], classOf[String],
      classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
  }
}
