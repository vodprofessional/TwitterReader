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

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweet") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text", O.NotNull, O.DBType("VARCHAR(150)"))
    def term = column[String]("term", O.NotNull, O.DBType("VARCHAR(150)"))
    def tweetedAt = column[Date]("tweetedAt", O.NotNull)
    def tweetId = column[String]("tweetId", O.NotNull, O.DBType("VARCHAR(30)"))
    def tweeterId = column[Long]("tweeter_id", O.NotNull)
    def retweets = column[Long]("retweets")
    def favorites = column[Long]("favorites")
    def * = (id.?, text, term, tweetedAt, tweetId, tweeterId, retweets, favorites) <> (Tweet.tupled, Tweet.unapply)
    def idx = index("tweetid_idx", tweetId, unique = true)
    def tweeter = foreignKey("tweeter_id", tweeterId, TableQuery[Tweeters])(_.id)
  }

  class Tweeters(tag: Tag) extends Table[Tweeter](tag, "tweeter") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def screenName = column[String]("screen_name", O.NotNull, O.DBType("VARCHAR(255)"))
    def joinDate = column[Date]("join_date", O.NotNull)
    def location = column[String]("location", O.DBType("VARCHAR(255)"))
    def * = (id.?, screenName, joinDate, location) <> (Tweeter.tupled, Tweeter.unapply)
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
      for (table <- List("settings", "tweet", "tweeters", "services", "service_extras", "search_term") if MTable.getTables(table).list.isEmpty )
        yield table match {
          case "settings" => TableQuery[Settings].ddl
          case "tweet" => TableQuery[Tweets].ddl
          case "tweeters" => TableQuery[Tweeters].ddl
          case "services" => TableQuery[Services].ddl
          case "service_extras" => TableQuery[ServiceExtras].ddl
          case "search_term" => TableQuery[SearchTerms].ddl
        }
    )
      .foldLeft[Option[dbProfile.SchemaDescription]](None)( (ddls, ddl) => if (ddls.isDefined) Some((ddls.get ++ ddl)) else Some(ddl) )
      .exists( ddl => { ddl.create; true } )

}
