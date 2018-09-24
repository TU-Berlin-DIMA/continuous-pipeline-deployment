package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class TaxiLocalProfile extends Profile {
  override val INPUT_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/processed/initial-training"
  override val STREAM_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/processed/stream"
  override val MATERIALIZED_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/processed/materialized"
  override val EVALUATION_PATH = "prequential"
  override val RESULT_PATH = "/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi-local/param-selectionn"
  override val INITIAL_PIPELINE = "/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi-local/pipelines/best/adam-0.001"
  override val NUM_FEATURES = 0
  override val NUM_ITERATIONS: Int = 5000
  override val SLACK = 120
  override val DAYS = "32,731"
  override val SAMPLE_SIZE = 720
  override val DAY_DURATION = 720
  override val PIPELINE_NAME = "taxi"
  override val PROFILE_NAME = "taxi-local"
  override val BATCH_EVALUATION: String = "/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/processed/batch-evaluation"
  override val TRAINING_FREQUENCY = 720
  override val ROLLING_WINDOW = 720 * 3
  override val MATERIALIZED_WINDOW = 100
  override val SAMPLING_STRATEGY = "time-based"
}
