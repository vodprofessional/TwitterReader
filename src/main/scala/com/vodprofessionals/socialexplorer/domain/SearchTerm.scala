package com.vodprofessionals.socialexplorer.domain

import java.util.Date

case class SearchTerm(id: Option[Long],
                      term: String,
                      createdAt: Date,
                      termType: SearchTermType,
                      containerId: Long,
                      status: String = "active")

sealed trait SearchTermType { val name = ""; def getName = name }
case object StaticSearchTerm extends SearchTermType { override val name = "static" }
case object DynamicSearchTerm extends SearchTermType { override val name = "dynamic" }


