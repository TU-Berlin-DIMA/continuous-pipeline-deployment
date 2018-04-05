package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class URLProfile extends Profile {
  val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
  val STREAM_PATH = "data/url-reputation/processed/stream"
  val EVALUATION_PATH = "prequential"
  val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
  val INITIAL_PIPELINE = "data/url-reputation/pipelines/best/adam"
  override val DELIMITER = ","
  // val NUM_FEATURES = 3231961
  val NUM_FEATURES = 3000
  override val NUM_ITERATIONS = 10000
  val SLACK = 5
  // 44 no error all 4400 rows are ok
  // 45 error but 3900 rows are only ok
  val DAYS = "1,120"
  override val DAY_DURATION = 100
  override val PIPELINE_NAME = "url-rep"
  override val REG_PARAM = 0.001
  override val SAMPLE_SIZE = 100
  override val CONVERGENCE_TOL = 1E-7

  override val PROFILE_NAME = "url"
  override val STEP_SIZE = 0.01
  override val MINI_BATCH = 1.0
}

