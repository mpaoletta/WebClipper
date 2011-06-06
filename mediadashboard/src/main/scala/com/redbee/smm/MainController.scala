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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MainController extends ScalatraFilter with ScalateSupport {

  val logger = LoggerFactory.getLogger(getClass);

  startCamelService

  val twitterService = actorOf(new com.redbee.smm.twitter.TwitterServiceActor).start

  implicit val TrackFormat: Format[Track] = asProduct3("guide", "keywords", "users")(Track)(Track.unapply(_).get)
  //implicit val DiscardFormat: Format[Discard] = as("guide")(Discard)(Discard.unapply(_).get)

  implicit val MetricsFormat: Format[Metric] = asProduct9("point", "tweets", "tweetsPos", "tweetsNeutral", "tweetsNeg", "tweetsP", "tweetsPPos", "tweetsPNeutral", "tweetsPNeg")(Metric)(Metric.unapply(_).get)

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
        <li>/metrics: Metricas simuladas</li>
        <li>/restart</li>
        <li>/guides/:guidename/track?keywords;key1,key2,key3&users;userId1,userId2</li>
        <li>/guides/:guidename/discard</li>
    	<li>/query?guides;guide1,guide2&aggregation;[day|hour|minute]&till;[yyyyMMdd|yyyyMMddHHmm|now]&span;numberofpoints</li>
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
    val keywords = params.get("keywords") match {
      case None => new Array[String](0)
      case Some(s) => s.split(',')
    }
    val users = params.get("users") match {
      case None => new Array[Long](0)
      case Some(s) => s.split(',').map(_.toLong)
    }
    val trackInfo = new Track(params("guide"), keywords, users)
    twitterService !! trackInfo match {
      case Some(true) => tojson(trackInfo).toString
      case Some(false) => renderStatus("ERROR", "No more keyword/user slots available")
      case None => renderStatus("ERROR", "Timeout")
    }
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
    renderStatus("OK", "restarting")
  }

  get("/guides") {
    contentType = "application/json"
    twitterService !! "guides" match {
      case Some(s:Iterable[String]) =>{
        //"""{"guides":[""" + s.asInstanceOf[Iterable[String]].reduceLeft(_ + ", " + _) + "]}"
        tojson(TwitterStorageAndMetricsDAO.getGuidesTrackInfo)
      }
      case None => renderStatus("ERROR", "")
    }
    
    //serializer out guides.get.asInstanceOf[Iterable[String]]
  }

  get("/getmetrics") {
    contentType = "application/json"
    val guideList = params("guides").split(',').toList
    val aggregation: String = params("aggregation")
    val parser = { if (List("hourly", "realtime").contains(aggregation)) sdtf else sdf }
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
    val till = {
		if(params("till") == "now") {
		  new java.util.Date
		}
	    else {
		    val parser = { if (List("hourly", "realtime", "minute").contains(aggregation)) sdtf else sdf }
		    parser.parse(params("till"))
	    }
    }
    val mts = RealTwitterMetrics.metricsFor(guideList, aggregation, span, till)
    tojson(mts).toString
  }

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
  
  private def renderStatus(status: String, msg: String): String = {
    """{"status":""" + status + """, "message":""" + msg + """}"""
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
    val tweetsNeutral = randomHasta(tweets - tweetsPos)
    val tweetsNeg = tweets - (tweetsPos + tweetsNeutral)
    val tweetsP = randomHasta(500000)
    val tweetsPPos = randomHasta(tweetsP)
    val tweetsPNeutral = randomHasta(tweetsP - tweetsPPos)
    val tweetsPNeg = tweetsP - (tweetsPPos + tweetsPNeutral)

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


