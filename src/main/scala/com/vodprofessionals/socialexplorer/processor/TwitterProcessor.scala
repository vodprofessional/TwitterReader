package com.vodprofessionals.socialexplorer.processor

import _root_.java.util.Date

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.jackson.JsonMethods._


/**
 *
 */
class TwitterProcessor(
                        val storage: Tweet => Unit
                       ) extends LazyLogging {


  /*
   *
   */
  def process(rawTweet: String) = rawTweet match {
    case jsonMessage: String =>
      val json = parse(jsonMessage)
      val tweet = parseTweet(json, SearchTerms.matchTerms(jsonMessage))
      val tweeter = getTweeter(json)

      // Any processing of the Tweet data comes here...
      if (tweet.term.length > 0) {
        storage(tweet)
      }
  }

  /**
   * Parse a Tweet out of Twitter's stream hose JSON response
   *
   * @param json The raw string containing the JSON object
   * @return Tweet
   */
  def parseTweet(json: org.json4s.JValue, matchingTerms: Set[String]): Tweet = {
    val dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", java.util.Locale.US)
    val text: String = { json \ "text" } match {
      case JString(s) => s
      case _ => ""
    }

    Tweet(
      None,
      text,
      matchingTerms.mkString(","),
      { json \ "created_at" } match {
        case JString(dateText:String) => dateFormat.parse(dateText)
        case _ => new Date(0L)
      },
      { json \ "id_str" } match {
        case JString(s) => s
        case _ => ""
      },
      -1,                         // TODO Somehow we need to use None here
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

  /**
   * Get the Tweeter user account data
   *
   * @param json
   */
  def getTweeter(json: org.json4s.JValue) = {
    Tweeter(
      None,
      { json \ "user" \ "screen_name" } match {
        case JString(s) => s
        case _ => ""
      },
      new java.util.Date(), // TODO Parse date
      ""  // TODO Parse location
    )
  }
}
