package com.redbee.smm.twitter

import java.util.Date
import com.novus.salat.annotations._


case class Tweet(@Key("_id") id: Long, author: User, inReplyToUserId: Long, text: String, retweetCount: Long, createdAt: Date, hashtags: List[String]
, rate: Int, neg: Int, neu: Int, pos: Int, guides: List[String]) {
  
  def enrich(_rate: Int, _neg: Int, _neu: Int, _pos: Int, _guides: List[String]): Tweet = {
    new Tweet(id, author, inReplyToUserId, text, retweetCount, createdAt, hashtags, _rate, _neg, _neu, _pos, _guides)
  }
  
}

case class User(@Key("_id") id: Long, screenName: String, statusesCount: Long, followersCount: Long
    , favouritesCount: Long, friendsCount: Long, location: Option[String], listedCount: Long
    , description: Option[String], timeZone: Option[String], verified: Boolean, lang: Option[String], name: String
    , createdAt: Date, profileImageURL: Option[String])    