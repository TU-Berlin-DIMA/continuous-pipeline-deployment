package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
object URLProfile extends Profile {
  val INPUT_PATH = "data/url-reputation/processed/initial-training/day_0"
  val STREAM_PATH = "data/url-reputation/processed/stream"
  val EVALUATION_PATH = "prequential"
  val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
  val INITIAL_PIPELINE = "data/url-reputation/pipelines/sampling-mode/pipeline-3000"
  val DELIMITER = ","
  // val NUM_FEATURES = 3231961
  val NUM_FEATURES = 3000
  val NUM_ITERATIONS = 2000
  val SLACK = 5
  // 44 no error all 4400 rows are ok
  // 45 error but 3900 rows are only ok
  val DAYS = "1,120"
  val SAMPLING_RATE = 0.1
  val DAY_DURATION = 100
  val PIPELINE_NAME = "url-rep"
  val REG_PARAM = 0.001
  val SAMPLE_SIZE = 100

  override val PROFILE_NAME = "url"
}
