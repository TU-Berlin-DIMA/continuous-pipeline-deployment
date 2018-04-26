package de.dfki.experiments.profiles

/**
  * @author behrouz
  */
abstract class Profile {
  val INPUT_PATH: String
  val STREAM_PATH: String
  val MATERIALIZED_PATH: String
  val EVALUATION_PATH: String
  val RESULT_PATH: String
  val INITIAL_PIPELINE: String
  val DELIMITER: String = ","
  val NUM_FEATURES: Int
  val NUM_ITERATIONS: Int = 2000
  val SLACK: Int
  val DAYS: String
  val SAMPLE_SIZE: Int
  val DAY_DURATION: Int
  val PIPELINE_NAME: String
  val REG_PARAM: Double = 0.001
  val CONVERGENCE_TOL: Double = 1E-6
  val MINI_BATCH: Double = 0.1
  val STEP_SIZE: Double = 0.001
  val UPDATER: String = "adam"
  val BATCH_EVALUATION: String = ""
  val NUM_PARTITIONS: Int = 8
  val TRAINING_FREQUENCY: Int

  val PROFILE_NAME: String
}

object Profile {
  val availableProfiles: List[Profile] = List(new CriteoClusterProfile(), new URLProfile(), new TaxiClusterProfile(), new CriteoLocalProfile(),
    new TaxiLocalProfile())

  def getProfile(name: String, default: Profile): Profile = {
    val profile = availableProfiles.filter(_.PROFILE_NAME == name)
    if (profile.isEmpty) default else profile.head
  }
}
