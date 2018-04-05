package de.dfki.experiments

import de.dfki.experiments.profiles.URLProfile
import de.dfki.ml.optimization.updater._
import de.dfki.utils.CommandLineParser
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Hyperparameter tuning
  * train a maximum of 1000 iterations models with lowest generalization error are considered as candidates
  *
  * @author behrouz
  */
object ParameterSelection extends Experiment {
  val UPDATERS: List[Updater] = List(
    new SquaredL2UpdaterWithAdam())

  val REGULARIZATIONS: List[Double] = List(0.001)

  val STEP_SIZES: List[Double] = List(0.01)

  val BATCH_EVALUATION = "data/url-reputation/processed/stream/day_1"

  override val defaultProfile = new URLProfile {
    override val RESULT_PATH = "../../../experiment-results/url-reputation/param-selection"
    override val INITIAL_PIPELINE = "data/url-reputation/pipelines/param-selection"
    override val NUM_FEATURES = 3000
    override val CONVERGENCE_TOL = 1E-7
    override val NUM_ITERATIONS = 10000
    override val DAYS = "1,10"
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
          params.initialPipeline = s"$rootPipelines/${u.name}-$s-$r"
          val pipeline = getPipeline(ssc.sparkContext, params)
          val matrix = pipeline.score(evaluationSet)
          println(s"${matrix.score()}")
          //val file = new File(s"${params.resultPath}/training")
          //file.getParentFile.mkdirs()
          //val fw = new FileWriter(file, true)
//          try {
//            fw.write(s"${u.name}(${params.stepSize}),$r,${matrix.rawScore()},${pipeline.getConvergedAfter}\n")
//          }
//          finally {
//            fw.close()
//          }
        }
      }
    }
    // hyper parameter evaluation for deployment
    //    for (u <- UPDATERS) {
    //      params.updater = u
    //      val deploymentParams = URLBestStochastic(params)
    //      val pipeline = getPipeline(ssc.sparkContext, deploymentParams)
    //      new ContinuousDeploymentQualityAnalysis(history = params.inputPath,
    //        streamBase = deploymentParams.streamPath,
    //        evaluation = s"${deploymentParams.evaluationPath}",
    //        resultPath = s"${deploymentParams.resultPath}",
    //        daysToProcess = deploymentParams.days,
    //        slack = params.slack,
    //        sampler = new TimeBasedSampler(size = deploymentParams.sampleSize)).deploy(ssc, pipeline)
    //    }
  }

  /**
    * manually prepared function, it contains the best pipeline found
    * for every learning rate adaptation technique
    *
    * @param params name of the learning rate adaptation technique
    * @return
    */
  def URLBestPipelines1000Iteration(params: Params): Params = {
    //  adam(0.01) 1e-03 0.02840 1000
    //  rmsprop(0.001) 1e-03 0.02745 1000
    //  adadelta(0.001) 1e-04 0.02685 1000
    //  momentum(0.01) 1e-03 0.03530 1000
    val copiedParams = params
    params.updater.name match {
      case "adadelta" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdaDelta()
        copiedParams.regParam = 0.0001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adadelta"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/adadelta-best"
        copiedParams
      case "adam" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdam()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adam"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/adam-best"
        copiedParams
      case "rmsprop" =>
        copiedParams.updater = new SquaredL2UpdaterWithRMSProp()
        copiedParams.stepSize = 0.001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/rmsprop"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/rmsprop-best"
        copiedParams
      case "momentum" =>
        copiedParams.updater = new SquaredL2UpdaterWithMomentum()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/momentum"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/momentum-best"
        copiedParams
    }
  }

  def URLBestPipelinesConvergence(params: Params): Params = {
    //  adam(1.0E-4) 1e-03 0.02725  1190
    //  rmsprop(0.001) 1e-03 0.02730  2030
    //  adadelta(0.001) 1e-03 0.02695 10000
    //  momentum(0.01) 1e-03 0.03530 1000
    val copiedParams = params
    params.updater.name match {
      case "adadelta" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdaDelta()
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adadelta"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/adadelta-best"
        copiedParams
      case "adam" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdam()
        copiedParams.stepSize = 0.0001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adam"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/adam-best"
        copiedParams
      case "rmsprop" =>
        copiedParams.updater = new SquaredL2UpdaterWithRMSProp()
        copiedParams.stepSize = 0.001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/rmsprop"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/rmsprop-best"
        copiedParams
      case "momentum" =>
        copiedParams.updater = new SquaredL2UpdaterWithMomentum()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/momentum"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/momentum-best"
        copiedParams
    }
  }

  def URLBest300000Features(params: Params): Params = {
    //  adam(0.001) 0.001 0.02595  3190
    //  rmsprop(1.0E-4) 0.001 0.02555 20000
    //  adadelta(0.001) 0.001 0.02655 20000
    //  momentum(0.01) 1e-03 0.03530 1000
    val copiedParams = params
    params.updater.name match {
      case "adadelta" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdaDelta()
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adadelta"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/adadelta"
        copiedParams
      case "adam" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdam()
        copiedParams.stepSize = 0.001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adam"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/adam"
        copiedParams
      case "rmsprop" =>
        copiedParams.updater = new SquaredL2UpdaterWithRMSProp()
        copiedParams.stepSize = 0.0001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/rmsprop"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/rmsprop"
        copiedParams
      case "momentum" =>
        copiedParams.updater = new SquaredL2UpdaterWithMomentum()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/momentum"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/momentum"
        copiedParams
    }
  }

  def URLBestStochastic(params: Params): Params = {
    //  adam(0.01) 1e-03 0.02680 10000
    //  rmsprop(0.001) 1e-03 0.02705 10000
    //  adadelta(0.001) 1e-03 0.02690 10000
    //  momentum(0.01) 1e-04 0.02995 10000
    val copiedParams = params
    params.updater.name match {
      case "adadelta" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdaDelta()
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adadelta"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/adadelta"
        copiedParams
      case "adam" =>
        copiedParams.updater = new SquaredL2UpdaterWithAdam()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/adam"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/adam"
        copiedParams
      case "rmsprop" =>
        copiedParams.updater = new SquaredL2UpdaterWithRMSProp()
        copiedParams.stepSize = 0.001
        copiedParams.regParam = 0.001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/rmsprop"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/rmsprop"
        copiedParams
      case "momentum" =>
        copiedParams.updater = new SquaredL2UpdaterWithMomentum()
        copiedParams.stepSize = 0.01
        copiedParams.regParam = 0.0001
        copiedParams.resultPath = s"${defaultProfile.RESULT_PATH}/momentum"
        copiedParams.initialPipeline = s"${defaultProfile.INITIAL_PIPELINE}/best/momentum"
        copiedParams
    }
  }
}
