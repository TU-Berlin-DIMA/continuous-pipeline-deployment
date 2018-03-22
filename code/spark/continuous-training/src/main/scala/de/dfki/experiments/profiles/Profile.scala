package de.dfki.experiments.profiles

import de.dfki.experiments.SamplingModes

/**
  * @author behrouz
  */
abstract class Profile {
  val INPUT_PATH: String
  val STREAM_PATH: String
  val EVALUATION_PATH: String
  val RESULT_PATH: String
  val INITIAL_PIPELINE: String
  val DELIMITER: String
  val NUM_FEATURES: Int
  val NUM_ITERATIONS: Int
  val SLACK: Int
  val DAYS: String
  val SAMPLE_SIZE: Int
  val DAY_DURATION: Int
  val PIPELINE_NAME: String
  val REG_PARAM: Double

  val PROFILE_NAME: String
}

object Profile {
  val availableProfiles: List[Profile] = List(CriteoClusterProfile, URLProfile, SamplingModes.DefaultProfile)

  def getProfile(name: String): Profile = {
    availableProfiles.filter(_.PROFILE_NAME == name).head
  }
}
