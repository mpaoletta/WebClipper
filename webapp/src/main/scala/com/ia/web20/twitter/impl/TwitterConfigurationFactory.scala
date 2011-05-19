package com.ia.web20.twitter.impl

import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import java.util.Properties


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