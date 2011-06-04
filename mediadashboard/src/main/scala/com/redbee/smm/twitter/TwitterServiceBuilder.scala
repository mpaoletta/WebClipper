package com.redbee.smm.twitter


//import cc.spray._
//import akka.actor.Actor._
//import scala.collection.mutable.ListBuffer
//import sjson.json._
//import DefaultProtocol._
//import sjson.json._
//import JsonSerialization._
//import java.util.Date
//import java.util.Calendar
//import java.text.SimpleDateFormat
//
//// TODO medir impacto de tipo de tweet, para poder actuar
//
//
//
//class TwitterServiceBuilder extends ServiceBuilder {
//  
//  val commaSplitter = com.google.common.base.Splitter.on(',')
//  val sdf = new SimpleDateFormat("yyyyMMdd")
//
///* 
//  point: String
//    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
//    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long
//*/
//  implicit val MetricsFormat: Format[Metric] = asProduct9("point", "tweets", "tweetsPos", "tweetsNeutral", "tweetsNeg", "tweetsP"
//         , "tweetsPPos", "tweetsPNeutral", "tweetsPNeg")(Metric)(Metric.unapply(_).get)
//  
//  implicit val SeriesFormat: Format[Series] = asProduct2("name", "data")(Series)(Series.unapply(_).get)
//         
//  val twitterService = {
//    
//    path("") {
//      get { _.complete("Twitter Service!") }
//    } ~
//    path("metrics") {
//      get { _.complete(tojson(MockTwitterMetrics.metrics).toString) }
//    } ~
//    path("track") {
//        (get & parameters('guide, 'keywords)) { 
//          (guide, keywords) => {      
//            registry.actorsFor("com.ia.web20.twitter.impl.TwitterService").head ! new Track(guide, keywords.split(','))
//        	_.complete("Siguiendo guia " + guide + "(" + keywords + ")" ) }
//          }
//    } ~
//    path("discard") { 
//      (get & parameter('guide)) { 
//        (guide) => {
//        	registry.actorsFor("com.ia.web20.twitter.impl.TwitterService").head ! new Discard(guide)
//        	_.complete("Descartando " + guide) 
//        	}
//        }      
//    } ~
//    path("getmetrics") { 
//      (get & parameters('guides, 'aggregation, 'till)) { 
//        (guides, aggregation, till) => {
//          val guideList = guides.split(',').toList
//          // Debe haber una forma para que haga el marshalling solo...
//          _.complete(tojson(MockTwitterMetrics.metricsFor(guideList, aggregation, sdf.parse(till))).toString)     
//        }
//      }
//    }
//  } 
//  
//}
//
//object MockTwitterMetrics extends MetricsQuery {
//  
//  val random = new scala.util.Random
//  
//  def metrics(): List[Series] = {
//    val series: List[String] = List("Coca Cola", "Pepsi")
//    metricsFor(series, "2weeks", sdf.parse("20110515"))
//  }
//  
//  def genMetrics(point: String, guide: String, aggregation: Aggregation): Metric = {
//    
//	  val tweets = randomHasta(5000)
//	  val tweetsPos = randomHasta(tweets)
//	  val tweetsNeutral = randomHasta(tweets-tweetsPos)
//	  val tweetsNeg = tweets-(tweetsPos+tweetsNeutral)
//	  val tweetsP = randomHasta(500000)
//	  val tweetsPPos = randomHasta(tweetsP)
//	  val tweetsPNeutral = randomHasta(tweetsP-tweetsPPos)
//	  val tweetsPNeg = tweetsP-(tweetsPPos+tweetsPNeutral)
//	  
//	  // escribir de nuevo
//	  val metrics = (tweets, tweetsPos, tweetsNeutral, tweetsNeg, tweetsP, tweetsPPos, tweetsPNeutral, tweetsPNeg)
//	  new Metric(point, metrics._1, metrics._2, metrics._3, metrics._4, metrics._5, metrics._6, metrics._7, metrics._8)
//  }
//
//  
//  def randomHasta(maximo: Long): Long = {
//    (random.nextDouble * maximo).longValue
//  }
//  
//  
//}