package com.vodprofessionals.socialexplorer.domain

import java.util.Date

case class SearchTerm(id: Option[Long],
                      term: String,
                      createdAt: Date,
                      ownerId: Int)

case class AddSearchTerm(terms: List[String])

/**
 *
 */
object SearchTerms {

}
