package com.vodprofessionals.socialexplorer

import java.sql.Timestamp
import org.json4s.jackson.JsonMethods._
import org.json4s.JString
import hu.lazycat.scala.slick.ContextAwareRDBMSProfile
import org.json4s.JsonAST.JInt

/**
 *
 */
trait DomainComponent { this: ContextAwareRDBMSProfile =>
  import dbProfile.simple._

  case class Tweet(text: String,
                   tweeter: String,
                   term: String,
                   tweetedAt: Timestamp,
                   tweetId: String,
                   retweets: Int = 0,
                   favorites: Int = 0,
                   id: Int = 0)
  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweet") {
    def text = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweeter = column[String]("tweeter", O.NotNull, O.DBType("VARCHAR(50)"))
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweetedAt = column[Timestamp]("tweetedAt", O.NotNull)
    def tweetId = column[String]("tweetId", O.NotNull, O.DBType("VARCHAR(30)"))
    def retweets = column[Int]("retweets")
    def favorites = column[Int]("favorites")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (text, tweeter, term, tweetedAt, tweetId, retweets, favorites, id) <> (Tweet.tupled, Tweet.unapply)
    def idx = index("tweetid_idx", (tweetId), unique = true)

    /**
     * Parse a Tweet out of Twitter's stream hose JSON response
     *
     * @param jsonString The raw string containing the JSON object
     * @return Tweet
     */
    def parseFromJSON(jsonString: String) = { implicit terms:List[String] =>
      val dateFormat = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", java.util.Locale.US)
      val json = parse(jsonString)
      val text:String = { json \ "text" } match {
        case JString(s) => s
        case _ => ""
      }
      val termRegex = {"(?i)("+terms.mkString("|")+")"}.r
      val matchingTerms = (for(m <- termRegex.findAllIn(text)) yield m.toLowerCase).toSet

      Tweet(
        text,
        { json \ "user" \ "screen_name" } match {
            case JString(s) => s
            case _ => ""
          },
        matchingTerms.mkString(","),
        { json \ "created_at" } match {
            case JString(dateText:String) => new Timestamp(dateFormat.parse(dateText).getTime)
            case _ => new Timestamp(0L)
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
  val tweets = TableQuery[Tweets]


  case class SearchTerm(term: String, ownerId: Int, createdAt: Timestamp, id: Int = 0)
  class SearchTerms(tag: Tag) extends Table[SearchTerm](tag, "search_term") {
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def createdAt = column[Timestamp]("createdAt", O.NotNull)
    def ownerId = column[Int]("ownerId", O.NotNull)
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (term, ownerId, createdAt, id) <> (SearchTerm.tupled, SearchTerm.unapply)
  }
  val searchTerms = TableQuery[SearchTerms]
}
