package com.redbee.smm.twitter.dao

import scala.collection.JavaConverters._
import com.redbee.smm.twitter.Track
import com.redbee.smm.twitter.Tweet
import com.redis._
import java.text.SimpleDateFormat

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import redis.clients.jedis._

import com.redbee.smm.twitter.Metric

object TwitterStorageAndMetricsDAO {

  val redis: RedisClient = new RedisClient("localhost", 6379)
  val jedis = new Jedis("localhost");
  val GUIDES = "guides:"
  val GUIDES_MEMBERS = GUIDES + "members"
  val formatMinute = new SimpleDateFormat("yyyyMMddHHmm")
  val formatHour = new SimpleDateFormat("yyyyMMddHH")
  val formatDay = new SimpleDateFormat("yyyyMMdd")

  var keywordsxGuia: HashMap[String, HashSet[String]] = null

  def addTracked(trackInfo: Track): Unit = {

    redis.pipeline { p =>
      p.sadd(GUIDES_MEMBERS, trackInfo.guide)
      val twitterKeywordSet = twitterKeywordSetFor(trackInfo.guide)
      for (keyword <- trackInfo.twitterKeywords) {
        p.sadd(twitterKeywordSet, keyword)
      }
    }

  }

  def getTrackInfo: HashMap[String, HashSet[String]] = {

    if (keywordsxGuia == null) {
      var keywords = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashSet[String]]

      for (guide <- redis.smembers(GUIDES_MEMBERS).get) {
        for (keyword <- redis.smembers(twitterKeywordSetFor(guide.get)).get) {
          keywords.getOrElseUpdate(keyword.get, new scala.collection.mutable.HashSet[String]) += guide.get
        }
      }

      keywordsxGuia = keywords
    }
    keywordsxGuia

  }

  def discard(g: String) = {
    keywordsxGuia -= g

    redis.pipeline { p =>
      p.srem(GUIDES_MEMBERS, g)
      val twitterKeywordSet = twitterKeywordSetFor(g)

      for (keyword <- p.smembers(twitterKeywordSetFor(g))) {
        p.srem(twitterKeywordSet, keyword)
      }
      //      updateAvailable = true
    }
  }

  private def twitterPrefixFor(g: String): String = {
    GUIDES + g + ":twitter:"
  }

  private def twitterKeywordSetFor(g: String): String = {
    twitterPrefixFor(g) + "keywords"
  }

  def updateWithStatus(tweet: Tweet): Unit = {

    this updateMetrics tweet

    this store tweet

  }

  private def store(tweet: Tweet): Unit = {
    TweetDAO save tweet
  }

  private def updateMetrics(tweet: Tweet): Unit = {
    val date = tweet.createdAt
    val dayPost = formatDay format date
    val hourPost = formatHour format date
    val minPost = formatMinute format date

    redis.pipeline { p =>
      for (guide <- tweet.guides) {
        val preGuide = twitterPrefixFor(guide)
        for (post <- List(dayPost, hourPost, minPost)) {
          val prefix = preGuide + post

          p.hincrby(prefix, "tweets", 1)
          if (tweet.neg > 0) p.hincrby(prefix, "tweetsNeg", 1)
          else if (tweet.neu > 0) p.hincrby(prefix, "tweetsNeu", 1)
          else p.hincrby(prefix, "tweetsPos", 1)

          p.hincrby(prefix, "tweetsP", tweet.rate)
          p.hincrby(prefix, "tweetsPNeg", tweet.neg)
          p.hincrby(prefix, "tweetsPNeu", tweet.neu)
          p.hincrby(prefix, "tweetsPPos", tweet.pos)

          p.sadd(prefix + ":tweets", tweet.id)
        }
      }
    }
  }

  def metricsFor(point: String, guide: String): Metric = {

    val key: String = twitterPrefixFor(guide) + point
    println("key: " + key)

    //    if (redis.exists(key)) {
    //      var shash: Option[Map[String, String]] = None
    //      try {
    //        
    //    	  shash = redis.hgetall(key)
    //
    //      } 
    //      catch {
    //        case e:Throwable => e.printStackTrace
    //      }
    //
    //      shash match {
    //        case Some(hash: Map[String, String]) => {
    //          val nhash = hash.mapValues(_.toLong)
    //          new Metric(point, nhash.getOrElse("tweets", 0), nhash.getOrElse("tweetsPos", 0), nhash.getOrElse("tweetsNeutral", 0), nhash.getOrElse("tweetsNeg", 0), nhash.getOrElse("tweetsP", 0), nhash.getOrElse("tweetsPPos", 0), nhash.getOrElse("tweetsPNeutral", 0), nhash.getOrElse("tweetsPNeg", 0))
    //
    //        }
    //        case _ => {
    //          new Metric(point, 0, 0, 0, 0, 0, 0, 0, 0)
    //        }
    //      }
    //
    //    } else
    //      new Metric(point, 0, 0, 0, 0, 0, 0, 0, 0)

    val mapa: java.util.Map[String, String] = jedis.hgetAll(key)
    if (mapa == null) {
      new Metric(point, 0, 0, 0, 0, 0, 0, 0, 0)
    } else {
      val smap = mapa.asScala
      val nhash = smap.mapValues(_.toLong)
      new Metric(point, nhash.getOrElse("tweets", 0), nhash.getOrElse("tweetsPos", 0), nhash.getOrElse("tweetsNeutral", 0), nhash.getOrElse("tweetsNeg", 0), nhash.getOrElse("tweetsP", 0), nhash.getOrElse("tweetsPPos", 0), nhash.getOrElse("tweetsPNeutral", 0), nhash.getOrElse("tweetsPNeg", 0))
      
    }
  }

}

object TweetDAO extends SalatDAO[Tweet, ObjectId](collection = MongoConnection()("smm")("tweets"))

