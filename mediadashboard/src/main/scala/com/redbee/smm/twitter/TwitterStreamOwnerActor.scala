package com.redbee.smm.twitter

import akka.actor.Actor._
import akka.actor.Actor
import java.util.Date
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.HashtagEntity
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import java.util.Properties
import scala.collection.mutable.ListBuffer

case class TrackKeywords(keywords: Array[String])


class TwitterStreamOwnerActor extends Actor with StatusListener {
  
  println("Iniciando TwitterStreamOwnerActor")
  
    val conf = new TwitterConfigurationFactory("configuration.properties")
	val tsf = new TwitterStreamFactory(conf.buildConfiguration);
    var twitterStream: TwitterStream = null // TODO manejar desconexion
    var keywords: Array[java.lang.String] = null
    
    
    def receive = {
    	case Restart => restart
    
    	case TrackKeywords(keys) => {println("TrackKeywords: " + keys); keywords = keys; restart}
    }
    
    def restart: Unit = {
      
      if(keywords != null && keywords.length > 0) {
    	  if(twitterStream != null) {
    		  twitterStream.cleanUp
    		  twitterStream = null
    	  }
    	  twitterStream = tsf.getInstance
    	  twitterStream addListener this
    	  var fq = new FilterQuery
    	  fq.track(keywords)
    	  twitterStream filter fq
      }
      
    }
  
  
  	def onStatus(status: Status): Unit = {

	    val u = status.getUser
	    
	    val author = new User(u.getId, u.getScreenName, u.getStatusesCount, u.getFollowersCount
	        , u.getFavouritesCount, u.getFriendsCount, u.getLocation, u.getListedCount, u.getDescription
	        , u.getTimeZone, false, u.getLang, u.getName, u.getCreatedAt)
	
	    var hts = new ListBuffer[String]
	    for(ht <- status.getHashtagEntities) {
	      hts += ht.getText
	    }
	    val tweet = new Tweet(status.getId, author, status.getText, status.getRetweetCount, status.getCreatedAt
	        , hts.readOnly, 0, 0, 0, 0, null)  	  
  	  
	    
		Actor.registry.actorsFor("com.redbee.smm.twitter.TwitterServiceActor").head ! tweet
  	}


    /**
     * Called upon deletionNotice notices. Clients are urged to honor deletionNotice requests and discard deleted statuses immediately. At times, status deletionNotice messages may arrive before the status. Even in this case, the late arriving status should be deleted from your backing store.
     *
     * @param statusDeletionNotice the deletionNotice notice
     * @see <a href="http://apiwiki.twitter.com/Streaming-API-Documentation#ParsingResponses">Streaming API Documentation - Parsing Responses</a>
     * @since Twitter4J 2.1.0
     */
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
      println("onDeletionNotice: " + statusDeletionNotice.getStatusId)
    }

    /**
     * This notice will be sent each time a limited stream becomes unlimited.<br>
     * If this number is high and or rapidly increasing, it is an indication that your predicate is too broad, and you should consider a predicate with higher selectivity.
     *
     * @param numberOfLimitedStatuses an enumeration of statuses that matched the track predicate but were administratively limited.
     * @see <a href="http://apiwiki.twitter.com/Streaming-API-Documentation#TrackLimiting">Streaming API Documentation - Track Limiting</a>
     * @see <a href="http://apiwiki.twitter.com/Streaming-API-Documentation#ParsingResponses">- Parsing Responses</a>
     * @see <a href="http://groups.google.co.jp/group/twitter-development-talk/browse_thread/thread/15d0504b3dd7b939">Twitter Development Talk - Track API Limit message meaning</a>
     * @since Twitter4J 2.1.0
     */
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
      println("onTrackLimitationNotice: " + numberOfLimitedStatuses)
    }

    /**
     * Called upon location deletion messages. Clients are urged to honor deletion requests and remove appropriate geolocation information from both the display and your backing store immediately. Note that in some cases the location deletion message may arrive before a tweet that lies within the deletion range arrives. You should still strip the location data.
     *
     * @param userId       user id
     * @param upToStatusId up to status id
     * @since Twitter4J 2.1.9
     */
    def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {
      
    }
  
    def onException(ex: Exception): Unit = {
      ex.printStackTrace
      restart
    }
}

class TwitterConfigurationFactory(propertiesResourceName: String) {
	
	def buildConfiguration: Configuration = {
		val p = properties
		val cb = new ConfigurationBuilder()
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(p.getProperty("twitter.consumerKey"))
		  .setOAuthConsumerSecret(p.getProperty("twitter.consumerSecret"))
		  .setOAuthAccessToken(p.getProperty("twitter.accesstoken"))
		  .setOAuthAccessTokenSecret(p.getProperty("twitter.tokenSecret"))
		cb.build()
	}

	private def properties: Properties = {
		val p = new Properties()
		p.load(getClass().getClassLoader().getResourceAsStream(propertiesResourceName))
		p
	}
	
}