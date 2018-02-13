package de.dfki.ml.pipelines.urlrep

/**
  * @author behrouz
  */
object GlobalVariables {

  // index of real values info here: http://archive.ics.uci.edu/ml/machine-learning-databases/url/url.names
  val URLRepRealIndices = Array(4, 5, 6, 8, 11, 16, 17, 18, 19, 21, 22, 23, 30, 33, 35, 39, 41, 43, 55, 57, 59, 61, 63,
    65, 67, 69, 71, 73, 75, 77, 79, 81, 83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109, 111, 113, 120, 126,
    132, 134, 136, 138, 140, 142, 144, 146, 148, 150, 161, 194, 270, 7801)

  val NUM_LABELS = 1
  val NUM_REAL_FEATURES = URLRepRealIndices.length
  // from http://archive.ics.uci.edu/ml/datasets/URL+Reputation
  val NUM_FEATURES = 3231961

  case class URLRepRawType(label: Double, numerical: Array[Double], categorical: Array[String])

}
