package com.vodprofessionals.socialexplorer.domain

import java.util.Date

import org.json4s.JString
import org.json4s.JsonAST.JInt
import org.json4s.jackson.JsonMethods._

case class Tweet(id:        Option[Long],
                 text:      String,
                 term:      String,
                 tweetedAt: Date,
                 tweetId:   Long,
                 tweeterId: Long,
                 retweets:  Long,
                 favorites: Long,
                 replyToId: Option[Long])
