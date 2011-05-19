package com.ia.web20.twitter.impl

class TwitterMetrics {

}

case class Series(name: String, data: List[Metric])

case class Metric(point: String
    , tweets: Long, tweetsPos: Long, tweetsNeutral: Long, tweetsNeg: Long
    , tweetsP: Long, tweetsPPos: Long, tweetsPNeutral: Long, tweetsPNeg: Long)