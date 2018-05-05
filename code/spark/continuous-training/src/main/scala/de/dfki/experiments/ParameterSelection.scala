package de.dfki.experiments

import java.io.{File, FileWriter}

import de.dfki.core.sampling.TimeBasedSampler
import de.dfki.deployment.continuous.ContinuousDeploymentWithOptimizations
import de.dfki.experiments.profiles.URLProfile
import de.dfki.ml.optimization.updater._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Hyperparameter tuning
  * train a maximum of 10000 iterations models with lowest generalization error are considered as candidates
  *
  * @author behrouz
  */
object ParameterSelection extends Experiment {

  val REGULARIZATIONS: List[Double] = List(0.01, 0.001, 0.0001)
  val UPDATERS: List[Updater] = List(
    new SquaredL2UpdaterWithAdam(),
    new SquaredL2UpdaterWithRMSProp(),
    new SquaredL2UpdaterWithAdaDelta())


  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "../../../experiment-results/url-reputation/param-selection"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/param-selection"
    override val NUM_FEATURES = 3000
    override val CONVERGENCE_TOL = 1E-7
    override val NUM_ITERATIONS = 10000
    override val DAYS = "1,30"
    override val MINI_BATCH = 0.1
  }

  def main(args: Array[String]): Unit = {
    val params = getParams(args, defaultProfile)
    val conf = new SparkConf().setAppName("Hyperparameter Tuning")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)

    val ssc = new StreamingContext(conf, Seconds(1))
    val rootPipelines = params.initialPipeline
    val evalSet = ssc.sparkContext.textFile(params.batchEvaluationSet)
    val rootResult = params.resultPath

    // hyper parameter evaluation for batch training
    for (u <- UPDATERS) {
      for (r <- REGULARIZATIONS) {
        params.updater = u
        params.regParam = r
        params.initialPipeline = s"$rootPipelines/${params.updater.name}-$r"
        val pipeline = getPipeline(ssc.sparkContext, params)
        val score = pipeline.score(evalSet)
        val file = new File(s"$rootResult/training")
        file.getParentFile.mkdirs()
        val fw = new FileWriter(file, true)
        try {
          fw.write(s"${params.updater.name},$r,${score.rawScore()}\n")
        }
        finally {
          fw.close()
        }
      }
    }
    // hyper parameter evaluation for deployment
    for (u <- UPDATERS) {
      for (r <- REGULARIZATIONS) {
        params.updater = u
        params.regParam = r
        params.initialPipeline = s"$rootPipelines/${params.updater.name}-$r"
        params.resultPath = s"$rootResult/${params.updater.name}-$r"

        val pipeline = getPipeline(ssc.sparkContext, params)
        new ContinuousDeploymentWithOptimizations(history = params.inputPath,
          streamBase = params.streamPath,
          materializeBase = params.materializedPath,
          evaluation = params.evaluationPath,
          resultPath = params.resultPath,
          daysToProcess = params.days,
          slack = params.slack,
          sampler = new TimeBasedSampler(size = params.sampleSize)).deploy(ssc, pipeline)
      }
    }
  }
}