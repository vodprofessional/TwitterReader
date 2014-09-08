package com.vodprofessionals.socialexplorer.akka

import akka.actor.Actor
import com.vodprofessionals.socialexplorer.akka.SearchTermsActor.{RemoveSearchTerm, AddSearchTerm}
import com.vodprofessionals.socialexplorer.model.SearchTerms

/**
 *
 */
object SearchTermsActor {
  case class AddSearchTerm(term: String)
  case class RemoveSearchTerm(term: String)
}

class SearchTermsActor extends Actor {
  override def receive = {
    case AddSearchTerm(term) =>
      SearchTerms.addTerm(term)

    case RemoveSearchTerm(term) =>
      SearchTerms.removeTerm(term)
  }
}
