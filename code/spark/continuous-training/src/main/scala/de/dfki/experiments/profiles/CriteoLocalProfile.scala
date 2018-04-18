package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class CriteoLocalProfile extends Profile{
  override val INPUT_PATH = "data/criteo-full/experiments/initial-training/day0"
  override val STREAM_PATH = "data/criteo-full/experiments/stream"
  override val EVALUATION_PATH = "prequential"
  override val RESULT_PATH = "../../../experiment-results/criteo-full/quality/local"
  override val INITIAL_PIPELINE = "data/criteo-full/pipelines/quality/init_500"
  override val DELIMITER = ","
  override val NUM_FEATURES = 3000
  override val NUM_ITERATIONS = 500
  override val SLACK = 10
  override val DAYS = "1,5"
  override val SAMPLE_SIZE = 100
  override val DAY_DURATION = 100
  override val PIPELINE_NAME = "criteo"
  override val REG_PARAM = 0.001
  override val PROFILE_NAME = "criteo-local"
  override val CONVERGENCE_TOL = 1E-6
  override val STEP_SIZE = 0.0001
  override val MINI_BATCH = 0.1
}
