package de.dfki.examples

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar

import de.dfki.core.streaming.BatchFileInputDStream
import de.dfki.deployment.ContinuousClassifier.constantInputDStreaming
import de.dfki.ml.evaluation.LogisticLoss
import de.dfki.ml.optimization.SquaredL2UpdaterWithAdam
import de.dfki.ml.streaming.models.{HybridLR, HybridModel}
import de.dfki.preprocessing.parsers.CustomVectorParser
import de.dfki.utils.CommandLineParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

/**
  * @author behrouz
  */
object AdvancedBatchStreamExample {
  val BATCH_INPUT = "data/test/initial-training/0"
  val STREAM_INPUT = "data/test/1"
  val VALIDATION_INPUT = "data/test/validation"
  val TEMP_INPUT = "data/test/temp"
  val RESULT_PATH = "data/test/result"
  val MODEL_PATH = "data/test/model/1"
  val MICRO_DURATION = 4
  val SLACK = 20
  @transient val logger = Logger.getLogger(getClass.getName)


  def main(args: Array[String]): Unit = {

    val parser = new CommandLineParser(args).parse()
    val conf = new SparkConf().setAppName("Advanced Batch Stream Example")
    // if master is not set run in local mode
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)


    val batchPath = parser.get("batch-path", BATCH_INPUT)
    val streamPath = parser.get("stream-path", STREAM_INPUT)
    val validationPath = parser.get("validation-path", VALIDATION_INPUT)
    val modelPath = parser.get("model-path", MODEL_PATH)

    val miniBatchDuration = parser.getLong("micro-duration", MICRO_DURATION)
    val sgdSlack = parser.getLong("slack", SLACK)

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val experimentId = dateFormat.format(Calendar.getInstance().getTime)
    val tempPath = s"${parser.get("temp-path", TEMP_INPUT)}/$experimentId"
    val resultPath = s"${parser.get("result-path", RESULT_PATH)}/$experimentId"

    new File(s"$tempPath").mkdirs()
    val ssc = new StreamingContext(conf, Seconds(miniBatchDuration))
    val dataParser = new CustomVectorParser()

    def batch = ssc.sparkContext.textFile(s"$batchPath,$tempPath")
      .map(dataParser.parsePoint)
      .sample(withReplacement = false, 0.2)

    val stream = new BatchFileInputDStream[LongWritable, Text, TextInputFormat](ssc, streamPath)
      .map(_._2.toString)

    val testData = constantInputDStreaming(ssc, validationPath)

    val storeRDD = (rdd: RDD[String], time: Time, path: String) => {
      val hadoopConf = new Configuration()
      hadoopConf.set("mapreduce.output.basename", time.toString())
      rdd.map(str => (null, str)).saveAsNewAPIHadoopFile(s"$path", classOf[NullWritable], classOf[String],
        classOf[TextOutputFormat[NullWritable, String]], hadoopConf)
    }


    val storeLogisticLoss = (rdd: RDD[Double], path: String) => {
      val file = new File(s"$path/loss.txt")
      file.getParentFile.mkdirs()
      val fw = new FileWriter(file, true)
      try {
        val content = rdd.collect().head

        fw.write(s"$content\n")
      }
      finally fw.close()
    }


    val model = if (Files.exists(Paths.get(modelPath))) {
      logger.info("Loading model from disk!!")
      HybridModel.loadFromDisk(modelPath)
    } else {
      logger.info("Training a new model!!!")
      val m = new HybridLR()
        .setStepSize(0.001)
        .setUpdater(new SquaredL2UpdaterWithAdam(0.9, 0.999))
        .setMiniBatchFraction(0.2)
        .setNumIterations(500)
        .trainInitialModel(batch)
      m.getUnderlyingModel.clearThreshold()
      HybridModel.saveToDisk(modelPath, m)
      m
    }

    model.setMiniBatchFraction(1.0)
    model.setNumIterations(1)


    // proactive training
    stream
      .map(dataParser.parsePoint)
      // online training
      .transform(rdd => model.trainOn(rdd))
      // combining the last data items
//      .window(Seconds(sgdSlack), Seconds(sgdSlack))
//      // train on union of streaming and batch
//      .transform(rdd => model.trainOnHybrid(rdd, batch))
      // unparse
      .map(dataParser.unparsePoint)
      // store the data
      .foreachRDD((r, t) => storeRDD(r, t, tempPath))


    testData
      // parser the data
      .map(d => {
        val parsed = dataParser.parsePoint(d)
        (parsed.label, parsed.features)
      })
      // predict
      .transform(rdd => model.predictOnValues(rdd))
      // calculate logistic loss
      .map(pre => (LogisticLoss.logisticLoss(pre._1, pre._2), 1))
      // sum over logistic loss
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
      // find total logistic loss
      .map(v => v._1 / v._2)
      // store the logistic loss into file
      .foreachRDD(rdd => storeLogisticLoss(rdd, resultPath))


    ssc.start()
    ssc.awaitTermination()

  }

}
