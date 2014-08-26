package com.vodprofessionals.socialexplorer.persistence

import java.util.Date

import com.vodprofessionals.socialexplorer.domain.{Setting, SearchTerm, Tweet}
import hu.lazycat.scala.slick.{ContextAwareRDBMSConnection, ContextAwareRDBMSProfile}
import scala.slick.jdbc.meta.MTable


/**
 *
 */
trait SlickComponents { this: ContextAwareRDBMSProfile =>
  import dbProfile.simple._

  implicit lazy val JavaUtilDateMapper =
    MappedColumnType
      .base[java.util.Date, java.sql.Timestamp] (
        d => new java.sql.Timestamp(d.getTime),
        d => new java.util.Date(d.getTime))

  val DB = (new ContextAwareRDBMSConnection(dbProfile)).databaseObject


  class Settings(tag: Tag) extends Table[Setting](tag, "settings") {
    def name = column[String]("name", O.PrimaryKey, O.NotNull, O.DBType("VARCHAR(150)"))
    def value = column[String]("value", O.NotNull, O.DBType("VARCHAR(150)"))
    def * = (name, value) <> (Setting.tupled, Setting.unapply)
  }

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweet") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweeter = column[String]("tweeter", O.NotNull, O.DBType("VARCHAR(50)"))
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweetedAt = column[Date]("tweetedAt", O.NotNull, O.DBType("DATE"))
    def tweetId = column[String]("tweetId", O.NotNull, O.DBType("VARCHAR(30)"))
    def retweets = column[Int]("retweets")
    def favorites = column[Int]("favorites")
    def * = (id.?, text, tweeter, term, tweetedAt, tweetId, retweets, favorites) <>
      (( Tweet.apply(_: Option[Long], _: String, _: String, _: String, _: Date, _: String, _: Int, _: Int) ).tupled, Tweet.unapply)
    def idx = index("tweetid_idx", tweetId, unique = true)
  }

  class SearchTerms(tag: Tag) extends Table[SearchTerm](tag, "search_term") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def createdAt = column[Date]("createdAt", O.NotNull)
    def ownerId = column[Int]("ownerId", O.NotNull)
    def * = (id.?, term, createdAt, ownerId) <> (SearchTerm.tupled, SearchTerm.unapply)
  }


  /**
   *
   */
  def createTables(implicit session: Session) =
    (
      for (table <- List("settings", "tweet", "search_term") if MTable.getTables(table).list.isEmpty )
        yield table match {
          case "settings" => TableQuery[Settings].ddl
          case "tweet" => TableQuery[Tweets].ddl
          case "search_term" => TableQuery[SearchTerms].ddl
        }
    )
      .foldLeft[Option[dbProfile.SchemaDescription]](None)( (ddls, ddl) => if (ddls.isDefined) Some((ddls.get ++ ddl)) else Some(ddl) )
      .exists( ddl => { ddl.create; true } )

}
