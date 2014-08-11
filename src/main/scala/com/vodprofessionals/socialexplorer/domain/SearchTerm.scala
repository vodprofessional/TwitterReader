package com.vodprofessionals.socialexplorer.domain

import java.util.Date

case class SearchTerm(term: String,
                      ownerId: Int,
                      createdAt: Date,
                      id: Int = 0)

case class AddSearchTerm
