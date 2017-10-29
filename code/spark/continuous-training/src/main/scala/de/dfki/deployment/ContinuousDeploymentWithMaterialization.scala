package de.dfki.deployment

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.ml.pipelines.Pipeline
import de.dfki.preprocessing.parsers.CustomVectorParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext

/**
  * with statistics update and materialization
  *
  * @author behrouz
  */
class ContinuousDeploymentWithMaterialization(val history: String,
                                              val stream: String,
                                              val resultPath: String,
                                              val samplingRate: Double = 0.1,
                                              val slack: Long = 10) extends Deployment {

  override def deploy(streamingContext: StreamingContext, pipeline: Pipeline) = {
    // set materialization option to true
    pipeline.setMaterialization(true)

    val dataParser = new CustomVectorParser()
    // create rdd of the initial data that the pipeline was trained with
    val data = streamingContext.sparkContext
      .textFile(history)
      .map(dataParser.parsePoint)


    val streamingSource = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](streamingContext, stream)

    def historicalDataRDD(recentItems: RDD[LabeledPoint]) = {
      streamingContext.sparkContext.textFile(streamingSource.getProcessedFiles.mkString(","))
        .map(dataParser.parsePoint)
        .union(data)
        .union(recentItems)
        .sample(withReplacement = false, samplingRate)
        .cache()
    }

    pipeline.model.setMiniBatchFraction(1.0)
    pipeline.model.setNumIterations(1)
    var proactiveRDD: RDD[LabeledPoint] = null
    var time = 0
    while (!streamingSource.allFileProcessed()) {
      val rdd = streamingSource.generateNextRDD().get.map(_._2.toString).map(dataParser.parsePoint)

      if (proactiveRDD == null) {
        proactiveRDD = rdd
      } else {
        proactiveRDD = proactiveRDD.union(rdd)
      }
      if (time % slack == 0) {
        // train the pipeline
        val startTime = System.currentTimeMillis()
        val nextBatch = historicalDataRDD(proactiveRDD)
        pipeline.trainOnMaterialized(nextBatch)
        val endTime = System.currentTimeMillis()

        // compute and store the training time
        val trainingTime = endTime - startTime
        storeTrainingTimes(trainingTime, resultPath)
        proactiveRDD = null
      }
      time += 1
    }
  }

  def writeStreamToDisk(rdd: RDD[String], path: String, index: Int): Unit = {
    val hadoopConf = new Configuration()
    hadoopConf.set("mapreduce.output.basename", s"$index-")
    rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$path", classOf[NullWritable], classOf[String],
      classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
  }
}

