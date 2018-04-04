package de.dfki.ml.pipelines.nyc_taxi

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.nyc_taxi.GLOBAL_VARIABLES.{NYCTaxiRawType, NYC_DATETIME_FORMAT}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class NYCKaggleInputParser(delim: String = ",") extends Component[String, NYCTaxiRawType] {
  /**
    * id,
    * vendor_id,
    * pickup_datetime,
    * dropoff_datetime,
    * passenger_count,
    * pickup_longitude,
    * pickup_latitude,
    * dropoff_longitude,
    * dropoff_latitude,
    * store_and_fwd_flag,
    * trip_duration
    */
  override def transform(spark: SparkContext, input: RDD[String]) = {
    input.map {
      line =>
        val raws = line.split(delim)
        val vendorID = raws(1).toDouble
        val puTimestamp = NYC_DATETIME_FORMAT.parse(raws(2))
        val passengerCount = raws(4).toDouble
        val (puLat, puLon) = (raws(6).toDouble, raws(5).toDouble)
        val (doLat, doLon) = (raws(8).toDouble, raws(7).toDouble)
        val tripDuration = raws(10).toDouble
        NYCTaxiRawType(tripDuration, vendorID, passengerCount, puLat, puLon,
          doLat, doLon, puTimestamp)
    }
  }

  override def update(sparkContext: SparkContext, input: RDD[String]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[String]) = {
    transform(spark, input)
  }
}
