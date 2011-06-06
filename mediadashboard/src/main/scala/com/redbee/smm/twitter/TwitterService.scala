package com.redbee.smm.twitter

import akka.actor.Actor._
import akka.actor.Actor
import akka.camel.{ Message, Consumer }

import java.text.SimpleDateFormat
import com.redbee.smm.twitter.dao._
import akka.camel.CamelServiceManager._
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

case class Restart()
case class Track(guide: String, twitterKeywords: Array[String])
case class Discard(guide: String)
case class Update()

/**
 * Encargado de controlar el comportamiento del servicio de twitter
 */
class TwitterServiceActor extends Actor {

  val logger = LoggerFactory.getLogger(getClass);

  
  logger.info("TwitterServiceActor initializing")
  
  val twitterStreamOwner = actorOf(new TwitterStreamOwnerActor).start
  actorOf(new TwitterEventScheduler).start
  
  var updateAvailable = false
  var twitterDAO = com.redbee.smm.twitter.dao.TwitterStorageAndMetricsDAO
  //var keywordsxGuia = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashSet[String]]
  
  def receive = {

    case t: Track => {
      logger.info("Track: " + t.guide)
      track(t) // {guias += (g -> new Guide(g, ks.toArray))}
      self.reply(true)
    }

    case Discard(g) => {
      twitterDAO discard g
    }

    case Update => {
      if (updateAvailable) restart
    }

    case status: Tweet => updateWithTweet(status)

    case Restart => restart

    case "guides" => self.reply(twitterDAO.getTrackInfo.keySet)
    
  }
  

  private def restart: Unit = {
    twitterStreamOwner ! new TrackKeywords(twitterDAO.getTrackInfo.keySet.toArray)
    updateAvailable = false
  }

  private def track(trackInfo: Track): Boolean = {
    updateAvailable = twitterDAO addTracked trackInfo
    updateAvailable = true
    updateAvailable
  }

  private def updateWithTweet(tweet: Tweet): Unit = {

    val status = tweet.text.toLowerCase
    var guides = new scala.collection.mutable.HashSet[String]
    for (keyword <- twitterDAO.getTrackInfo.keys) {
      if (status.contains(keyword)) {
        guides ++= twitterDAO.getTrackInfo(keyword)
      }
    }

    val (pond, neg, neu, pos) = rate(tweet)

    val enriched = tweet.enrich(pond, neg, neu, pos, guides.toList)

    TwitterStorageAndMetricsDAO updateWithStatus enriched

  }

  val random = new scala.util.Random
  def rate(tweet: Tweet): (Int, Int, Int, Int) = {
    val rand = (random.nextDouble * 10).intValue
    val sentimiento = { if (rand < 2) (1, 0, 0) else if (rand < 8) (0, 1, 0) else (0, 0, 1) }
    val ponderacion = 1 //(tweet.user.followersCount + tweet.retweetCount).intValue
    (ponderacion, sentimiento._1 * ponderacion, sentimiento._2 * ponderacion, sentimiento._3 * ponderacion)
  }
}


