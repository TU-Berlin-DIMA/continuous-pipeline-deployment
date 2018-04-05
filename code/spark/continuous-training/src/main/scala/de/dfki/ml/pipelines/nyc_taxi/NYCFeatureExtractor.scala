package de.dfki.ml.pipelines.nyc_taxi

import java.util.Calendar

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.nyc_taxi.GLOBAL_VARIABLES.{NYCTaxiFeatures, NYCTaxiRawType}
import org.apache.commons.lang3.time.DateUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.math._

/**
  * @author behrouz
  */
class NYCFeatureExtractor extends Component[NYCTaxiRawType, NYCTaxiFeatures] {

  // add features about airport
  override def transform(spark: SparkContext, input: RDD[NYCTaxiRawType]) = {
    input.map {
      record =>
        val distance = haversine(record.puLat, record.puLon, record.doLat, record.doLon)
        val direction = bearing(record.puLat, record.puLon, record.doLat, record.doLon)
        val cal = DateUtils.toCalendar(record.puTimestamp)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val weekDay = cal.get(Calendar.DAY_OF_WEEK)

        NYCTaxiFeatures(record.tripDuration, record.vendorID, record.passengerCount, record.puLat, record.puLon,
          record.doLat, record.doLon, distance, direction, hour, weekDay)
    }
  }

  override def update(sparkContext: SparkContext, input: RDD[NYCTaxiRawType]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[NYCTaxiRawType]) = {
    transform(spark, input)
  }

  def haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = {
    val EARTH_RADIUS = 6372.8
    //radius in km
    val dLat = (lat2 - lat1).toRadians
    val dLon = (lon2 - lon1).toRadians
    val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat1.toRadians) * cos(lat2.toRadians)
    val c = 2 * asin(sqrt(a))
    EARTH_RADIUS * c
  }

  def bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = {
    val (rLat1, rLon1, rLat2, rLon2) = (lat1.toRadians, lon1.toRadians, lat2.toRadians, lon2.toRadians)
    val y = sin(rLon2 - rLon1) * cos(rLat2)
    val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(rLon2 - rLon1)

    (atan2(y, x) + 360).toDegrees % 360
  }
}
