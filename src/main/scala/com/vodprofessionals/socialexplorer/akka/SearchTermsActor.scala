package com.vodprofessionals.socialexplorer.akka

import _root_.java.util.Date

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.SearchTermsActor.{CheckForExpiredDynamicTerms, RemoveSearchTerm, AddSearchTerm}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import java.util.Calendar
import com.vodprofessionals.socialexplorer.persistence.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile, SlickComponents}
import scala.concurrent.duration._

import scala.slick.driver.JdbcProfile

/**
 *
 */
object SearchTermsActor {
  case class AddSearchTerm(term: String)
  case class RemoveSearchTerm(term: String)
  case object CheckForExpiredDynamicTerms
}

class SearchTermsActor extends Actor with LazyLogging {
  import context.dispatcher

  val expiredRefresher = context.system.scheduler.schedule(1 day, 1 day, self, CheckForExpiredDynamicTerms)


  override def postStop() = expiredRefresher.cancel()

  override def receive = {
    case AddSearchTerm(term) =>
      SearchTerms.addTerm(term)

    case RemoveSearchTerm(term) =>
      SearchTerms.removeTerm(term)

    case CheckForExpiredDynamicTerms =>
      class DAL(val dbProfile: JdbcProfile) extends SlickComponents with ContextAwareRDBMSProfile {
        import dbProfile.simple._

        def deactivateExpiredTerms(): Unit =
          DB withSession { implicit session: Session =>
            val cal = Calendar.getInstance()
            cal.setTime(new Date)
            cal.add(Calendar.DAY_OF_MONTH, -7)
            val expiredIds: List[Long] = TableQuery[ServiceExtras]
              .filter(_.txDateTime > cal.getTime)
              .map(e => e.id)
              .run
              .toList

            val tt = for(id <- expiredIds) yield {
              TableQuery[SearchTerms]
                .filter(_.containerId === id)
                .map(t => t.status)
                .update("disabled")
            }
          }

        def getNewTerms =
          DB withSession { implicit session: Session =>
            TableQuery[SearchTerms]
              .filter(_.status === "active")
              .groupBy(t => t.term)
              .map{ case (term, group) => (term) }.run.toSet
          }
      }
      val dal = (new DAL(ContextAwareRDBMSDriver.driver))
      dal.deactivateExpiredTerms
      SearchTerms.replaceTerms(dal.getNewTerms)
      logger.info("Checking for dynamic term expires - reloading if necessary")
  }
}
