package com.redbee.smm.twitter

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer

// Aggregation
case class Yearly() 
case class Semester() 
case class Quarterly() 
case class Monthly() 
case class TwoWeeks() 
case class Weekly() 
case class Hourly() 

case class Series(name: String, data: List[Metric])

case class Metric(point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long)
    
trait MetricsQuery {

  val sdf = new SimpleDateFormat("yyyyMMdd")
  val sdtf = new SimpleDateFormat("yyyyMMddHHmm")
  
  val formatDay = new SimpleDateFormat("dd/MM")
  val formatHour = new SimpleDateFormat("HH:mm")
  
  def metricsFor(series: List[String], aggregation: String, till: java.util.Date): List[Series] = {
    val traversable: Traversable[Series] = series.map(s => genSerie(s, aggregation, till))
    traversable.toList
  }

  /**
   * Nueva interfaz de metricas
   */
  def metricsFor(series: List[String], aggregation: String, span: Int, till: java.util.Date): List[Series] = {
    val traversable: Traversable[Series] = series.map(s => genSerie(s, aggregation, span, till))
    traversable.toList
  }
  
  
  def genSerie(guide: String, aggregation: String, span: Int, till: java.util.Date): Series = {
		val cal = java.util.Calendar.getInstance
		cal.setTime(till)

		val (calField, formatter) = aggregation match {
		  case "day" => (Calendar.DAY_OF_MONTH, sdf)
		  case "hour" => (Calendar.HOUR_OF_DAY, sdtf)
		  case "minute" => (Calendar.MINUTE, sdtf)
		}
		
		var lista = new ListBuffer[Metric]
		for(i <- 1 to span) {
		  lista += genMetrics(formatter format cal.getTime, guide)
		  cal.add(calField, -1)
		}
		new Series(guide, lista.toList.reverse)
  }
  
  /*
case class Metric(point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long)  
 */
  
  def genSerie(nombre: String, aggregation: String, till: java.util.Date): Series = {
		
		val cal = java.util.Calendar.getInstance
		cal.setTime(till)
		
		val (since: Date, period: String, agg: Any) = aggregation match {
		  case "2weeks" => {cal.add(Calendar.DAY_OF_MONTH, -14);(cal.getTime, "day", TwoWeeks)}
		  case "monthly" => {cal.add(Calendar.MONTH, -1);(cal.getTime, "day", Monthly)}
		  case "weekly" => {cal.add(Calendar.DAY_OF_MONTH, -7);(cal.getTime, "day", Weekly)}
		  case "hourly" => {cal.add(Calendar.HOUR, -24);(cal.getTime, "hour", Hourly)}
		  case "realtime" => {cal.add(Calendar.HOUR, -4);(cal.getTime, "hour", Hourly)}
		  case _ => {cal.add(Calendar.DAY_OF_MONTH, -7);(cal.getTime, "day", Weekly)}
		}
    
	    var lista = new ListBuffer[Metric]
		if("day" == period) {
			val daze: Int = ((till.getTime - since.getTime) / (24 * 60 * 60 * 1000)).intValue
			cal.setTime(till)
			for(i <- 1 to daze) {
				val point = sdf format cal.getTime
				lista+= genMetrics(point, nombre)
				cal.add(Calendar.DAY_OF_MONTH, -1)
			}
		}
		else {
			val hourz: Int = ((till.getTime - since.getTime) / (60 * 60 * 1000)).intValue
			cal.setTime(till)
			for(i <- 1 to hourz) {
				val point = sdtf format cal.getTime
				lista+=genMetrics(point, nombre)
				cal.add(Calendar.HOUR, -1)
			}		  
		}

	    new Series(nombre, lista.toList.reverse)
  }   
  
  def genMetrics(point: String, guide: String): Metric  
}