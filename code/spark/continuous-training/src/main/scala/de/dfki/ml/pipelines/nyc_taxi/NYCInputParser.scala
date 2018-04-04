package de.dfki.ml.pipelines.nyc_taxi

import java.text.SimpleDateFormat

import de.dfki.ml.pipelines.Component
import de.dfki.ml.pipelines.nyc_taxi.GLOBAL_VARIABLES._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
class NYCInputParser(delim: String = ",") extends Component[String, NYCTaxiRawType] {
  /** VendorID,
    * tpep_pickup_datetime,
    * tpep_dropoff_datetime,
    * passenger_count
    * trip_distance,
    * pickup_longitude,
    * pickup_latitude,
    * RateCodeID,
    * store_and_fwd_flag,
    * dropoff_longitude,
    * dropoff_latitude,
    * payment_type,
    * fare_amount,
    * extra,
    * mta_tax,
    * tip_amount,
    * tolls_amount,
    * improvement_surcharge,
    * total_amount
    * **/

  /**
    * parse the csv file and filter unnecessary columns
    *
    * @param spark
    * @param input
    * @return
    */
  override def transform(spark: SparkContext, input: RDD[String]): RDD[NYCTaxiRawType] = {
    //    input.map {
    //      line =>
    //        //print(line)
    //        val raws = line.split(delim)
    //        val vendorID = raws(0).toDouble
    //        val puTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(raws(1))
    //        val doTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(raws(2))
    //        val passengerCount = raws(3).toDouble
    //        val (puLon, puLat) = (raws(5).toDouble, raws(6).toDouble)
    //        val (doLon, doLat) = (raws(9).toDouble, raws(10).toDouble)
    //        val tripDuration = (doTimestamp.getTime - puTimestamp.getTime) / 1000
    //        NYCTaxiRawType(tripDuration, vendorID, passengerCount, puLat, puLon,
    //          doLat, doLon, puTimestamp)
    //    }
    input.mapPartitions { iter =>
      val FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      iter.map {
        line =>
          //print(line)
          val raws = line.split(delim)
          val vendorID = raws(0).toDouble
          val puTimestamp = FORMAT.parse(raws(1))
          val doTimestamp = FORMAT.parse(raws(2))
          val passengerCount = raws(3).toDouble
          val (puLon, puLat) = (raws(5).toDouble, raws(6).toDouble)
          val (doLon, doLat) = (raws(9).toDouble, raws(10).toDouble)
          val tripDuration = (doTimestamp.getTime - puTimestamp.getTime) / 1000
          NYCTaxiRawType(tripDuration, vendorID, passengerCount, puLat, puLon,
            doLat, doLon, puTimestamp)
      }
    }
  }

  override def update(sparkContext: SparkContext, input: RDD[String]) = {}

  override def updateAndTransform(spark: SparkContext, input: RDD[String]) = {
    transform(spark, input)
  }
}
