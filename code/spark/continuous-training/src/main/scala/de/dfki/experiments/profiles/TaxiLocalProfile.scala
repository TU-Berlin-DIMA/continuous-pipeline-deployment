package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class TaxiLocalProfile extends Profile {
  override val INPUT_PATH = "data/nyc-taxi/processed/initial-training"
  override val STREAM_PATH = "data/nyc-taxi/processed/stream"
  override val MATERIALIZED_PATH = "data/nyc-taxi/processed/materialized"
  override val EVALUATION_PATH = "prequential"
  override val RESULT_PATH = "/share/hadoop/behrouz/experiments/nyx-taxi/results/param-selection"
  override val INITIAL_PIPELINE = "/share/hadoop/behrouz/experiments/nyx-taxi/pipelines/best/adam"
  override val NUM_FEATURES = 0
  override val NUM_ITERATIONS: Int = 5000
  override val SLACK = 120
  override val DAYS = "32,731"
  override val SAMPLE_SIZE = 720
  override val DAY_DURATION = 720
  override val PIPELINE_NAME = "taxi"
  override val PROFILE_NAME = "taxi-local"
  override val BATCH_EVALUATION: String = "data/nyc-taxi/processed/batch-evaluation"
  override val TRAINING_FREQUENCY = 720
}
