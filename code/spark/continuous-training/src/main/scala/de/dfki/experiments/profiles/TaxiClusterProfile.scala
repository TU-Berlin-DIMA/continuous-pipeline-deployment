package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
class TaxiClusterProfile extends Profile {
  override val INPUT_PATH = "hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/initial-training"
  override val STREAM_PATH = "hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/stream"
  override val MATERIALIZED_PATH = "hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/materialized"
  override val EVALUATION_PATH = "prequential"
  override val RESULT_PATH = "/share/hadoop/behrouz/experiments/nyc-taxi/results/param-selection"
  override val INITIAL_PIPELINE = "/share/hadoop/behrouz/experiments/nyc-taxi/pipelines/best/rmsprop-1.0E-4"
  override val NUM_FEATURES = 0
  override val NUM_ITERATIONS: Int = 5000
  override val SLACK = 24
  //override val DAYS = "32,731"
  //TODO the format of the file is changed after 547 days, we should fix the code to accommodate for that as well
  override val DAYS = "32,547"
  override val SAMPLE_SIZE = 720
  override val DAY_DURATION = 720
  override val NUM_PARTITIONS = 320
  override val STEP_SIZE = 0.001
  override val PIPELINE_NAME = "taxi"
  override val PROFILE_NAME = "taxi-cluster"
  override val BATCH_EVALUATION: String = "hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/batch-evaluation"
  override val TRAINING_FREQUENCY = 720
}
