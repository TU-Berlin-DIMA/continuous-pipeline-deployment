package de.dfki.ml.pipelines.nyc_taxi

import java.text.SimpleDateFormat
import java.util.Date

/**
  * @author behrouz
  */
object GLOBAL_VARIABLES {

  case class NYCTaxiRawType(tripDuration: Double, vendorID: Double, passengerCount: Double, puLat: Double,
                            puLon: Double, doLat: Double, doLon: Double, puTimestamp: Date)

  case class NYCTaxiFeatures(tripDuration: Double, vendorID: Double, passengerCount: Double, puLat: Double,
                             puLon: Double, doLat: Double, doLon: Double, distance: Double, direction: Double,
                             hour: Double, weekDay: Double) {
    def features(): Array[Double] = {
      Array[Double](vendorID, passengerCount, puLat, puLon, doLat, doLon, distance, direction, hour, weekDay)
    }
  }


  val NYC_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

}
