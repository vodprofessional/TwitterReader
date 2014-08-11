package com.vodprofessionals.socialexplorer.persistence

import java.sql.Timestamp

import com.vodprofessionals.socialexplorer.domain.{SearchTerm, Tweet, System}
import hu.lazycat.scala.slick.ContextAwareRDBMSProfile


/**
 *
 */
trait SlickComponents { this: ContextAwareRDBMSProfile =>
  import dbProfile.simple._

  class SystemVariables(tag: Tag) extends Table[System](tag, "system") {
    def key = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def value = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def * = (key, value) <> (System.tupled, System.unapply)
    def idx = index("key_idx", (key), unique = true)
  }
  val SYSTEM = TableQuery[SystemVariables]

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
  }
  val TWEETS = TableQuery[Tweets]


  class SearchTerms(tag: Tag) extends Table[SearchTerm](tag, "search_term") {
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def createdAt = column[Timestamp]("createdAt", O.NotNull)
    def ownerId = column[Int]("ownerId", O.NotNull)
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (term, ownerId, createdAt, id) <> (SearchTerm.tupled, SearchTerm.unapply)
  }
  val SEARCH_TERMS = TableQuery[SearchTerms]
}
