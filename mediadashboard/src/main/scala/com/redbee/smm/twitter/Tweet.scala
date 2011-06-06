package com.redbee.smm.twitter

import java.util.Date

case class Tweet(id: Long, author: User, inReplyToUserId: Long, text: String, retweetCount: Long, createdAt: Date, hashtags: List[String]
, rate: Int, neg: Int, neu: Int, pos: Int, guides: List[String]) {
  
  def enrich(_rate: Int, _neg: Int, _neu: Int, _pos: Int, _guides: List[String]): Tweet = {
    new Tweet(id, author, inReplyToUserId, text, retweetCount, createdAt, hashtags, _rate, _neg, _neu, _pos, _guides)
  }
  
}

case class User(id: Long, screenName: String, statusesCount: Long, followersCount: Long
    , favouritesCount: Long, friendsCount: Long, location: String, listedCount: Long
    , description: String, timeZone: String, verified: Boolean, lang: String, name: String
    , createdAt: Date)    