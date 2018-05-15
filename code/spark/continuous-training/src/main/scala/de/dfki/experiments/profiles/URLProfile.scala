package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class URLProfile extends Profile {
  val INPUT_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed/initial-training"
  val STREAM_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed/stream"
  val MATERIALIZED_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed/materialized"
  val EVALUATION_PATH = "prequential"
  val RESULT_PATH = "../../../experiment-results/url-reputation/sampling"
  val INITIAL_PIPELINE = "/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/pipelines/best/adam-0.001"
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
  override val CONVERGENCE_TOL = 1E-10
  override val NUM_PARTITIONS = 8

  override val PROFILE_NAME = "url"
  override val STEP_SIZE = 0.001
  override val MINI_BATCH = 1.0
  override val BATCH_EVALUATION = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed/batch-evaluation"
  override val TRAINING_FREQUENCY = 1000
}

