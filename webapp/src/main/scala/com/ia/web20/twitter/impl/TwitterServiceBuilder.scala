package com.ia.web20.twitter.impl

import cc.spray._
import scala.collection.mutable.ListBuffer
import sjson.json._
import DefaultProtocol._
import sjson.json._
import JsonSerialization._

// TODO medir impacto de tipo de tweet, para poder actuar

class TwitterServiceBuilder extends ServiceBuilder {
  
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
    }
    
    path("metrics") {
      get { _.complete(tojson(MockTwitterMetrics.metrics).toString) }
    }
    
  } 
  
}

object MockTwitterMetrics {
  
  val random = new scala.util.Random
  
  def metrics: List[Series] = {
    val series: List[String] = List("Coca Cola", "Pepsi")
    val traversable: Traversable[Series] = series.map(s => genSerie(s))
    traversable.toList
  }
  
/*
case class Metric(point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long)  
 */
  
  def genSerie(nombre: String): Series = {
	    var lista = new ListBuffer[Metric]
	    for(i <- 1 to 20) {
	      val tweets = randomHasta(5000)
	      val tweetsPos = randomHasta(tweets)
	      val tweetsNeutral = randomHasta(tweets-tweetsPos)
	      val tweetsNeg = tweets-(tweetsPos+tweetsNeutral)
	      val tweetsP = randomHasta(500000)
	      val tweetsPPos = randomHasta(tweetsP)
	      val tweetsPNeutral = randomHasta(tweetsP-tweetsPPos)
	      val tweetsPNeg = tweetsP-(tweetsPPos+tweetsPNeutral)
	      
	      lista += new Metric("2011/05/" + {if(i<10) "0" + i else i}, tweets, tweetsPos, tweetsNeutral, tweetsNeg
	          , tweetsP, tweetsPPos, tweetsPNeutral, tweetsPNeg)
	    }
	    new Series(nombre, lista.toList)
  }   

  
  def randomHasta(maximo: Long): Long = {
    (random.nextDouble * maximo).longValue
  }
  
}