package com.redbee.smm

import org.scalatra._
import java.net.URL
import scalate.ScalateSupport
import akka.actor.Actor._
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import com.redbee.smm.twitter._
import com.redbee.smm.twitter.dao._
import akka.camel.CamelServiceManager._

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import java.text.SimpleDateFormat

class MainController extends ScalatraFilter with ScalateSupport {
    
  startCamelService

  val twitterService = actorOf(new com.redbee.smm.twitter.TwitterServiceActor).start

  implicit val TrackFormat: Format[Track] = asProduct2("guide", "keywords")(Track)(Track.unapply(_).get)
  //implicit val DiscardFormat: Format[Discard] = as("guide")(Discard)(Discard.unapply(_).get)

  implicit val MetricsFormat: Format[Metric] = asProduct9("point", "tweets", "tweetsPos", "tweetsNeutral", "tweetsNeg", "tweetsP"
         , "tweetsPPos", "tweetsPNeutral", "tweetsPNeg")(Metric)(Metric.unapply(_).get)
  
  implicit val SeriesFormat: Format[Series] = asProduct2("name", "data")(Series)(Series.unapply(_).get)
  
  private val serializer = Serializer.SJSON
  
  val sdf = new SimpleDateFormat("yyyyMMdd")
  val sdtf = new SimpleDateFormat("yyyyMMddHHmm")
  
  get("/") {
    contentType = "text/html"
    <html>
      <body>
        <h1>Media dashboard</h1>
        Operaciones:
    	<li>/metrics</li>
    	<li>/restart</li>
    	<li>/:guide/track?keywords=key1,key2,key3</li>
    	<li>/:guide/discard</li>
      </body>
    </html>
  }
  
  get("/metrics") {
	contentType = "application/json"
	tojson(MockTwitterMetrics.metrics).toString
  }
  
//    path("track") {
//        (get & parameters('guide, 'keywords)) { 
//          (guide, keywords) => {      
//            registry.actorsFor("com.ia.web20.twitter.impl.TwitterService").head ! new Track(guide, keywords.split(','))
//        	_.complete("Siguiendo guia " + guide + "(" + keywords + ")" ) }
//          }
//    } ~
  get("/guides/:guide/track") {
    contentType = "application/json"
    val trackInfo = new Track(params("guide"), params("keywords").split(','))
    twitterService ! trackInfo
    tojson(trackInfo).toString
  }
  
  get("/guides/:guide/discard") {
    contentType = "application/json"
    val discard = new Discard(params("guide"))
    twitterService ! discard
    """{"guide":""" + params("guide") + """"}"""
  }
  
  get("/restart") {
    contentType = "application/json"
    twitterService ! Restart
    """{"status":"OK"}"""
  }
  
  get("/guides") {
    contentType = "application/json"
    val guides = (twitterService !! "guides")
    //serializer out guides.get.asInstanceOf[Iterable[String]]
    """{"guides":[""" + guides.get.asInstanceOf[Iterable[String]].reduceLeft(_ + ", " + _) + "]}"
  }

  //    path("getmetrics") { 
//      (get & parameters('guides, 'aggregation, 'till)) { 
//        (guides, aggregation, till) => {
//          val guideList = guides.split(',').toList
//          // Debe haber una forma para que haga el marshalling solo...
//          _.complete(tojson(MockTwitterMetrics.metricsFor(guideList, aggregation, sdf.parse(till))).toString)     
//        }
//      }
//    }
  
  get("/getmetrics") {
    contentType = "application/json"
    val guideList = params("guides").split(',').toList
    val aggregation: String = params("aggregation")
    val parser = {if(List("hourly","realtime").contains(aggregation)) sdtf else sdf}
    val till = parser.parse(params("till"))
    val mts = RealTwitterMetrics.metricsFor(guideList, aggregation, till)
    tojson(mts).toString
  }
  
  get("/getmockmetrics") {
    contentType = "application/json"
    val guideList = params("guides").split(',').toList
    val aggregation = params("aggregation")
    val till = sdf.parse(params("till"))
    tojson(MockTwitterMetrics.metricsFor(guideList, aggregation, till)).toString
  }
  
  get("/query") {
    contentType = "application/json"
    val guideList = params("guides").split(',').toList
    val aggregation: String = params("aggregation")
    val span: Int = params("span").toInt
    val parser = {if(List("hourly","realtime","minute").contains(aggregation)) sdtf else sdf}
    val till = parser.parse(params("till"))
    val mts = RealTwitterMetrics.metricsFor(guideList, aggregation, span, till)
    tojson(mts).toString  }
  
  
  notFound {
    // If no route matches, then try to render a Scaml template
    val templateBase = requestPath match {
      case s if s.endsWith("/") => s + "index"
      case s => s
    }
    val templatePath = "/WEB-INF/scalate/templates/" + templateBase + ".scaml"
    servletContext.getResource(templatePath) match {
      case url: URL => 
        contentType = "text/html"
        templateEngine.layout(templatePath)
      case _ => 
        filterChain.doFilter(request, response)
    } 
  }
  
}

object MockTwitterMetrics extends MetricsQuery {
  
  val random = new scala.util.Random
  
  def metrics(): List[Series] = {
    val series: List[String] = List("Coca Cola", "Pepsi")
    metricsFor(series, "2weeks", sdf.parse("20110515"))
  }
  
  def genMetrics(point: String, guide: String): Metric = {
    
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

object RealTwitterMetrics extends MetricsQuery {


  def genMetrics(point: String, guide: String): Metric = {
	 TwitterStorageAndMetricsDAO.metricsFor(point, guide)
  }
  
}


