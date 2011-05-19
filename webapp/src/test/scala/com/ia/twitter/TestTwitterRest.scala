package com.ia.twitter

import scala.collection.JavaConverters._

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener
import twitter4j.TwitterException;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import com.ia.web20.twitter.impl.TwitterConfigurationFactory
import java.util.Calendar
import java.text.SimpleDateFormat

object TestTwitterRest {
  
  main(new Array[String](0))
  
  def main(args : Array[String]) : Unit = {
    
    val searchTerms = Array("adidas", "nike", "puma") //"cocacola", "coca", "coca cola", "pepsi");
    
    val conf = new TwitterConfigurationFactory("configuration.properties")
    
		val tsf = new TwitterFactory(conf.buildConfiguration);
		val twitter = tsf.getInstance();
		
		var first = false
		val query = new StringBuilder
		for(term <- searchTerms) {
			if(!first) 
			  query.append(" OR ")
			 query.append(term)
		}
		
		var cal = Calendar.getInstance
		cal.add(Calendar.DAY_OF_MONTH, -15)
		val sinceDate = {if(args.length > 0) args(0) else new SimpleDateFormat("yyyy-MM-dd") format cal.getTime}
		
		var q = new Query(query toString)
		q setRpp 100
		q since sinceDate
		//q setLang "es"
		
		var count = 0
		var lastId = 0l
		var uptodate = false
		
		val userMap = new scala.collection.mutable.HashMap[String, Int]
		
		while(!uptodate) {
			var results = twitter search q			
			
			if(results.getWarning != null) {
				println("WARNING: " + results.getWarning)
				//uptodate = true
			}
			 
			if(results.getTweets.size == 0) {
				println("No more tweets in query")
				uptodate = true
			}
			else {
				val tweets = results.getTweets.asScala
				
				val usersToLookup = new scala.collection.mutable.HashSet[String]
				for(status <- tweets) {
					val userName = status.getFromUser
					if(!userMap.contains(userName)) {
					  usersToLookup add userName
					}
					if(usersToLookup.size % 100 == 0)
					  lookupUsers(twitter, usersToLookup)
				}				
				lookupUsers(twitter, usersToLookup)
				
				for(status <- tweets) {
					var buffer = new StringBuilder();
										
					val followerCount = userMap.getOrElse(status.getFromUser, ((_: String) => -1))
					//buffer.append(status.getFromUser).append(" (").append(status.getUser().getFollowersCount()).append(")\n");

					buffer.append(status.getFromUser()).append(" (").append(-1).append(")\n");
					buffer.append(status.getText());
					println(buffer.toString());
					lastId = Math.max(status.getId, lastId)
					count = count + 1
				}
				q setPage results.getPage + 1
			}
		}
		println ("Tweets obtenidos: " + count)
			
		/*
		if(lastId > 0)
				q.sinceId(lastId);
			
			q.setRpp(100);
			
			QueryResult result 
		
		
		val fq = new FilterQuery();
		fq.track(searchTerms);
		twitterStream.filter(fq);
		Thread.sleep(60000l);
		*/
  }
  
  def lookupUsers(twitter: Twitter, users: Traversable[String]): Unit = {
    
  }
  
}
