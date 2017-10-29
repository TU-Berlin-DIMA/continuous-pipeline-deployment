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
                                              val resultPath: String,
                                              val numIterations: Int = 500) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    val days = (1 to 5).map(i => s"$stream/day_$i")
    var copyPipeline = pipeline

    val parser = new CustomVectorParser()
    var trainingDays: ListBuffer[String] = new ListBuffer[String]()
    trainingDays += history

    for (day <- days) {
      copyPipeline = copyPipeline.newPipeline()
      copyPipeline.model.setNumIterations(numIterations)
      trainingDays += day

      val data = streamingContext.sparkContext
        .textFile(trainingDays.mkString(","))
        .map(parser.parsePoint)

      val startTime = System.currentTimeMillis()
      copyPipeline.trainOnMaterialized(data)
      val endTime = System.currentTimeMillis()

      storeTrainingTimes(endTime - startTime, resultPath)
    }
  }

  def writeStreamToDisk(rdd: RDD[String], path: String, index: Int): Unit = {
    val hadoopConf = new Configuration()
    hadoopConf.set("mapreduce.output.basename", s"$index-")
    rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$path", classOf[NullWritable], classOf[String],
      classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
  }
}
