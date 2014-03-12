package com.vodprofessionals.socialexplorer

import hu.lazycat.scala.slick.ContextAwareRDBMSProfile
import java.sql.Date

/**
 *
 */
trait DomainComponent { this: ContextAwareRDBMSProfile =>
  import dbProfile.simple._

  case class Tweet(text: String, tweeter: String, term: String, tweetedAt: Date, retweets: Int = 0, favorites: Int = 0, id: Int = 0)
  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweet") {
    def text = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweeter = column[String]("tweeter", O.NotNull, O.DBType("VARCHAR(50)"))
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweetedAt = column[Date]("tweetedAt", O.NotNull)
    def retweets = column[Int]("retweets")
    def favorites = column[Int]("favorites")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * =  (text, tweeter, term, tweetedAt, retweets, favorites, id) <> (Tweet.tupled, Tweet.unapply)
  }
  val tweets = TableQuery[Tweets]
}
