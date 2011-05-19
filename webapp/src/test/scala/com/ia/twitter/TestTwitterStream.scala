package com.ia

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import com.ia.web20.twitter.impl.TwitterConfigurationFactory

object TestTwitterStream {
  def main(args : Array[String]) : Unit = {
    
    val searchTerms = Array("adidas", "nike", "puma") //"cocacola", "coca", "coca cola", "pepsi");
    
    val conf = new TwitterConfigurationFactory("configuration.properties")
    
		val tsf = new TwitterStreamFactory(conf.buildConfiguration);
		val twitterStream = tsf.getInstance();
		twitterStream.addListener(new StatusStreamListener(twitterStream));
		val fq = new FilterQuery();
		fq.track(searchTerms);
		twitterStream.filter(fq);
		Thread.sleep(60000l);
  }
}

	class StatusStreamListener(twitterStream: TwitterStream) extends StatusListener {
		def onTrackLimitationNotice(arg0: Int): Unit = {
			println("onTrackLimitationNotice(" + arg0 + ")");
			twitterStream.cleanUp();
		}

		def onException(arg0: Exception): Unit =  {
			println("onException", arg0);
			twitterStream.cleanUp();
			
		}

		def onDeletionNotice(arg0: StatusDeletionNotice): Unit =  {
			println("onDeletionNotice: " + arg0);
			twitterStream.cleanUp();
		}

		def onScrubGeo(arg0: Long, arg1: Long): Unit =  {
			println("onScrubGeo: " + arg0 + ", " + arg1);
			twitterStream.cleanUp();
		}

		def onStatus(status: Status): Unit =  {
			var buffer = new StringBuilder();
			buffer.append(status.getUser().getName()).append(" (").append(status.getUser().getFollowersCount()).append(")\n");
			buffer.append(status.getText());
			println(buffer.toString());
		}
		
	}