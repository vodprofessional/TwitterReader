package com.vodprofessionals.socialexplorer.processor

import _root_.java.util.Date

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import org.json4s.JsonAST.{JObject, JBool, JInt, JString}
import org.json4s.jackson.JsonMethods._


/**
 *
 */
class TwitterProcessor(
                        val storage: (Tweet, Tweeter) => Unit
                       ) extends LazyLogging {

  val dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", java.util.Locale.US)


  /*
   *
   */
  def process(rawTweet: String) = rawTweet match {
    case jsonMessage: String =>
      val json = parse(jsonMessage)
      val retweetedStatus = { json \ "retweeted_status" } match {
        case s: JObject => Some(s)
        case _ => None
      }
      val terms = SearchTerms.matchTerms(jsonMessage)

      if (terms.size > 0) {
        for (term <- terms) yield storage(parseTweet(retweetedStatus.getOrElse(json), term), parseTweeter(retweetedStatus.getOrElse(json)))
      }
  }

  /**
   * Parse a Tweet out of Twitter's stream hose JSON response
   *
   * @param json The raw string containing the JSON object
   * @return Tweet
   */
  def parseTweet(json: org.json4s.JValue, matchingTerm: String): Tweet =
    Tweet(
      { json \ "id" } match {
        case JInt(s) => s.toLong
        case _ => 0L
      },
      { json \ "text" } match {
        case JString(s) => s
        case _ => ""
      },
      matchingTerm,
      { json \ "created_at" } match {
        case JString(dateText:String) => dateFormat.parse(dateText)
        case _ => new Date(0L)
      },
      { json \ "user" \ "id"} match {
        case JInt(s) => s.toLong
        case _ => 0L
      },
      { json \ "retweet_count" } match {
        case JInt(i) => i.toLong
        case _ => 0L
      },
      { json \ "favorite_count" } match {
        case JInt(i) => i.toLong
        case _ => 0L
      },
      { json \ "in_reply_to_status_id" } match {
        case JInt(i) => Some(i.toLong)
        case _ => None
      }
    )


  /**
   *
   * @param json
   */
  def parseTweeter(json: org.json4s.JValue): Tweeter = {
    Tweeter(
      { json \ "user" \ "id" } match {
        case JInt(s) => s.toLong
        case _ => 0L
      },
      { json \ "user" \ "screen_name" } match {
        case JString(s) => s
        case _ => ""
      },
      { json \ "user" \ "created_at" } match {
        case JString(s) => dateFormat.parse(s)
        case _ => new Date(0L)
      },
      { json \ "user" \ "location" } match {
        case JString(s) => s
        case _ => ""
      },
      { json \ "user" \ "followers_count" } match {
        case JInt(s) => s.toLong
        case _ => 0L
      }
    )
  }

}
