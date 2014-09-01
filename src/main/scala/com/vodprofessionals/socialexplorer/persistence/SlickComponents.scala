package com.vodprofessionals.socialexplorer.persistence

import java.util.Date

import com.vodprofessionals.socialexplorer.domain._
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

  implicit lazy val SearchTermTypeMapper =
    MappedColumnType
      .base[SearchTermType, String] (
      d => d.getName,
      d => d match {
        case "static" => StaticSearchTerm
        case "dynamic" => DynamicSearchTerm
      })

  val DB = (new ContextAwareRDBMSConnection(dbProfile)).databaseObject


  class Settings(tag: Tag) extends Table[Setting](tag, "settings") {
    def name = column[String]("name", O.PrimaryKey, O.NotNull, O.DBType("VARCHAR(150)"))
    def value = column[String]("value", O.NotNull, O.DBType("VARCHAR(150)"))
    def * = (name, value) <> (Setting.tupled, Setting.unapply)
  }

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweets") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweetedAt = column[Date]("tweetedAt", O.NotNull)
    def tweetId = column[Long]("tweetId", O.NotNull)
    def tweeterId = column[Long]("tweeter_id", O.NotNull)
    def retweets = column[Long]("retweets", O.NotNull)
    def favorites = column[Long]("favorites", O.NotNull)
    def replyToId = column[Long]("reply_to_id", O.Nullable)
    def * = (id.?, text, term, tweetedAt, tweetId, tweeterId, retweets, favorites, replyToId.?) <> (Tweet.tupled, Tweet.unapply)
    def idx = index("tweetid_idx", tweetId, unique = true)
    def tweeter = foreignKey("tweeter_id", tweeterId, TableQuery[Tweeters])(_.id)
  }

  class Tweeters(tag: Tag) extends Table[Tweeter](tag, "tweeters") {
    def id = column[Long]("id", O.PrimaryKey)
    def screenName = column[String]("screen_name", O.NotNull, O.DBType("VARCHAR(255)"))
    def joinDate = column[Date]("join_date", O.NotNull)
    def location = column[String]("location", O.NotNull, O.DBType("VARCHAR(255)"))
    def followersCount = column[Long]("followers_count", O.NotNull)
    def * = (id, screenName, joinDate, location, followersCount) <> (Tweeter.tupled, Tweeter.unapply)
  }

  class Services(tag: Tag) extends Table[Service](tag, "services") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(255)"))
    def * = (id.?, name) <> (Service.tupled, Service.unapply)
  }

  class ServiceExtras(tag: Tag) extends Table[ServiceExtra](tag, "service_extras") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull, O.DBType("VARCHAR(255)"))
    def txDateTime = column[Date]("tx_datetime", O.NotNull)
    def channel = column[String]("channel", O.NotNull, O.DBType("VARCHAR(255)"))
    def serviceId = column[Long]("service_id", O.NotNull)
    def * = (id.?, name, txDateTime, channel, serviceId) <> (ServiceExtra.tupled, ServiceExtra.unapply)
  }

  class SearchTerms(tag: Tag) extends Table[SearchTerm](tag, "search_terms") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def createdAt = column[Date]("createdAt", O.NotNull)
    def termType = column[SearchTermType]("term_type", O.NotNull, O.DBType("VARCHAR(30)"))
    def containerId = column[Long]("container_id", O.NotNull)
    def * = (id.?, term, createdAt, termType, containerId) <> (SearchTerm.tupled, SearchTerm.unapply)
    def idx = index("search_terms_type_containerid_idx", (termType, containerId), unique = true)
  }


  /**
   *
   */
  def createTables(implicit session: Session) =
    (
      for (table <- List("settings", "tweets", "tweeters", "services", "service_extras", "search_terms") if MTable.getTables(table).list.isEmpty )
        yield table match {
          case "settings"       => TableQuery[Settings].ddl
          case "tweets"         => TableQuery[Tweets].ddl
          case "tweeters"       => TableQuery[Tweeters].ddl
          case "services"       => TableQuery[Services].ddl
          case "service_extras" => TableQuery[ServiceExtras].ddl
          case "search_terms"   => TableQuery[SearchTerms].ddl
        }
    )
      .foldLeft[Option[dbProfile.SchemaDescription]](None)( (ddls, ddl) => if (ddls.isDefined) Some((ddls.get ++ ddl)) else Some(ddl) )
      .exists( ddl => { ddl.create; true } )

}