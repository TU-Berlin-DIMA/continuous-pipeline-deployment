package de.dfki.experiments

import java.io.{File, FileWriter}

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.ContinuousDeploymentQualityAnalysis
import de.dfki.experiments.profiles.URLProfile
import de.dfki.ml.optimization.updater._
import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  *
  * @author behrouz
  */
object ParameterSelection extends Experiment {
  val UPDATERS: List[Updater] = List(
    new SquaredL2UpdaterWithAdam(),
    new SquaredL2UpdaterWithRMSProp(),
    new SquaredL2UpdaterWithAdaDelta(),
    new SquaredL2UpdaterWithMomentum())

  val REGULARIZATIONS: List[Double] = List(0.001, 0.0001)

  val STEP_SIZES: List[Double] = List(0.01, 0.001, 0.0001)

  val BATCH_EVALUATION = "data/url-reputation/processed/stream/day_1"

  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "../../../experiment-results/url-reputation/param_selection"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/param-selection"
  }

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)
    val conf = new SparkConf().setAppName("Learning Rate Selection Criteo")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))

    // parser for extra parameters
    val parser = new CommandLineParser(args).parse()
    val evalSet = parser.get("eval-set", BATCH_EVALUATION)
    val evaluationSet = ssc.sparkContext.textFile(evalSet)
    val rootPipelines = params.initialPipeline

    // hyper parameter evaluation for batch training
    for (u <- UPDATERS) {
      for (r <- REGULARIZATIONS) {
        for (s <- if (u.name == "adadelta") List(0.001) else STEP_SIZES) {
          params.updater = u
          params.regParam = r
          params.stepSize = s
          params.initialPipeline = s"$rootPipelines/${u.name}-${params.stepSize}-$r"
          val pipeline = getPipeline(ssc.sparkContext, params)
          val matrix = pipeline.score(evaluationSet)
          val file = new File(s"${params.resultPath}/training")
          file.getParentFile.mkdirs()
          val fw = new FileWriter(file, true)
          try {
            fw.write(s"${u.name}(${params.stepSize}),$r,${matrix.rawScore()}\n")
          }
          finally {
            fw.close()
          }
        }
      }
    }
    // hyper parameter evaluation for deployment
    for (u <- UPDATERS) {
      for (r <- REGULARIZATIONS) {
        for (s <- if (u.name == "adadelta") List(0.001) else STEP_SIZES) {
          params.updater = u
          params.regParam = r
          params.stepSize = s
          params.initialPipeline = s"$rootPipelines/${u.name}(${params.stepSize})-$r"
          val pipeline = getPipeline(ssc.sparkContext, params)
          new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
            streamBase = params.streamPath,
            evaluation = s"${params.evaluationPath}",
            resultPath = s"${params.resultPath}",
            daysToProcess = params.days,
            slack = params.slack,
            sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, pipeline)
        }
      }
    }

  }

  /**
    * manually prepared function, it contains the best pipeline found
    * for every learning rate adaptation technique
    *
    * @param name name of the learning rate adaptation technique
    * @return
    */
  def URLBestPipelines(name: String): String = {
    name match {
      case "adadelta" => s"${defaultProfile.INITIAL_PIPELINE}-adadelta-0.001-0.001"
      case "adam" => s"${defaultProfile.INITIAL_PIPELINE}-adam-0.001-0.001"
      case "rmsprop" => s"${defaultProfile.INITIAL_PIPELINE}-rmsprop-1.0E-4-0.001"
      case "momentum" => s"${defaultProfile.INITIAL_PIPELINE}-momentum-0.001-0.001"
    }

  }
}
