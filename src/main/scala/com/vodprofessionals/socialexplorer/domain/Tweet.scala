package com.vodprofessionals.socialexplorer.domain

import java.util.Date

import org.json4s.JString
import org.json4s.JsonAST.JInt
import org.json4s.jackson.JsonMethods._

case class Tweet(text: String,
                 tweeter: String,
                 term: String,
                 tweetedAt: Date,
                 tweetId: String,
                 retweets: Int = 0,
                 favorites: Int = 0,
                 id: Int = 0)

case class RawTweet(message: String)

object Tweets {
  /**
   * Parse a Tweet out of Twitter's stream hose JSON response
   *
   * @param jsonString The raw string containing the JSON object
   * @return Tweet
   */
  def fromJSON(jsonString: String, matchingTerms: Set[String]) = {
    val dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", java.util.Locale.US)
    val json = parse(jsonString)
    val text:String = { json \ "text" } match {
      case JString(s) => s
      case _ => ""
    }

    Tweet(
      text,
      { json \ "user" \ "screen_name" } match {
        case JString(s) => s
        case _ => ""
      },
      matchingTerms.mkString(","),
      { json \ "created_at" } match {
        case JString(dateText:String) => dateFormat.parse(dateText)
        case _ => new Date(0L)
      },
      { json \ "id_str" } match {
        case JString(s) => s
        case _ => ""
      },
      { json \ "retweet_count" } match {
        case JInt(i) => i.intValue()
        case _ => 0
      },
      { json \ "favorite_count" } match {
        case JInt(i) => i.intValue()
        case _ => 0
      }
    )
  }
}
