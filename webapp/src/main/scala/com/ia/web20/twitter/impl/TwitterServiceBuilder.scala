package com.ia.web20.twitter.impl

import cc.spray._
import scala.collection.mutable.ListBuffer
import sjson.json._
import DefaultProtocol._
import sjson.json._
import JsonSerialization._
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat

// TODO medir impacto de tipo de tweet, para poder actuar

class TwitterServiceBuilder extends ServiceBuilder {
  
  val commaSplitter = com.google.common.base.Splitter.on(',')
  val sdf = new SimpleDateFormat("yyyyMMdd")

/* 
  point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long
*/
  implicit val MetricsFormat: Format[Metric] = asProduct9("point", "tweets", "tweetsPos", "tweetsNeutral", "tweetsNeg", "tweetsP"
         , "tweetsPPos", "tweetsPNeutral", "tweetsPNeg")(Metric)(Metric.unapply(_).get)
  
  implicit val SeriesFormat: Format[Series] = asProduct2("name", "data")(Series)(Series.unapply(_).get)
         
  val twitterService = {
    
    path("") {
      get { _.complete("Twitter Service!") }
    } ~
    path("metrics") {
      get { _.complete(tojson(MockTwitterMetrics.metrics).toString) }
    } ~
    path("track") {
        (get & parameters('guide, 'keywords)) { 
          (guia, keywords) => {        	  
        	  _.complete("Siguiendo guia " + guia + "(" + keywords + ")" ) }
          }
    } ~
    path("discard") { 
      (get & parameter('guide)) { 
        (guia) => _.complete("Descartando " + guia) 
        }      
    } ~
    path("getmetrics") { 
      (get & parameters('guides, 'aggregation, 'till)) { 
        (guides, aggregation, till) => {
          val guideList = guides.split(',').toList
          // Debe haber una forma para que haga el marshalling solo...
          _.complete(tojson(MockTwitterMetrics.metricsFor(guideList, aggregation, sdf.parse(till))).toString)     
        }
      }
    }
  } 
  
}

object MockTwitterMetrics {
  
  val random = new scala.util.Random
  val sdf = new SimpleDateFormat("yyyyMMdd")
  val formatDay = new SimpleDateFormat("dd/MM")
  val formatHour = new SimpleDateFormat("HH:mm")
  
  def metricsFor(series: List[String], aggregation: String, till: java.util.Date): List[Series] = {
    val traversable: Traversable[Series] = series.map(s => genSerie(s, aggregation, till))
    traversable.toList
  }
  
  def metrics(): List[Series] = {
    val series: List[String] = List("Coca Cola", "Pepsi")
    metricsFor(series, "2weeks", sdf.parse("20110515"))
  }

  /*
case class Metric(point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long)  
 */
  
  def genSerie(nombre: String, aggregation: String, till: java.util.Date): Series = {
		
		val cal = java.util.Calendar.getInstance
		cal.setTime(till)
		
		val (since: Date, period: String) = aggregation match {
		  case "2weeks" => {cal.add(Calendar.DAY_OF_MONTH, -14);(cal.getTime, "day")}
		  case "monthly" => {cal.add(Calendar.MONTH, -1);(cal.getTime, "day")}
		  case "weekly" => {cal.add(Calendar.DAY_OF_MONTH, -7);(cal.getTime, "day")}
		  case "hourly" => {cal.add(Calendar.HOUR, -24);(cal.getTime, "hour")}
		  case "realtime" => {cal.add(Calendar.HOUR, -4);(cal.getTime, "hour")}
		  case _ => {cal.add(Calendar.DAY_OF_MONTH, -7);(cal.getTime, "day")}
		}
    
	    var lista = new ListBuffer[Metric]
		if("day" == period) {
			val daze: Int = ((till.getTime - since.getTime) / (24 * 60 * 60 * 1000)).intValue
			cal.setTime(till)
			for(i <- 1 to daze) {
				val point = formatDay format cal.getTime
				lista+= genMetrics(point)
				cal.add(Calendar.DAY_OF_MONTH, -1)
			}
		}
		else {
			val hourz: Int = ((till.getTime - since.getTime) / (60 * 60 * 1000)).intValue
			cal.setTime(till)
			for(i <- 1 to hourz) {
				val point = formatHour format cal.getTime
				lista+=genMetrics(point)
				cal.add(Calendar.HOUR, -1)
			}		  
		}

	    new Series(nombre, lista.toList.reverse)
  }   
  
  def genMetrics(point: String): Metric = {
    
	  val tweets = randomHasta(5000)
	  val tweetsPos = randomHasta(tweets)
	  val tweetsNeutral = randomHasta(tweets-tweetsPos)
	  val tweetsNeg = tweets-(tweetsPos+tweetsNeutral)
	  val tweetsP = randomHasta(500000)
	  val tweetsPPos = randomHasta(tweetsP)
	  val tweetsPNeutral = randomHasta(tweetsP-tweetsPPos)
	  val tweetsPNeg = tweetsP-(tweetsPPos+tweetsPNeutral)
	  
	  // escribir de nuevo
	  val metrics = (tweets, tweetsPos, tweetsNeutral, tweetsNeg, tweetsP, tweetsPPos, tweetsPNeutral, tweetsPNeg)
	  new Metric(point, metrics._1, metrics._2, metrics._3, metrics._4, metrics._5, metrics._6, metrics._7, metrics._8)
  }

  
  def randomHasta(maximo: Long): Long = {
    (random.nextDouble * maximo).longValue
  }
  
  
}