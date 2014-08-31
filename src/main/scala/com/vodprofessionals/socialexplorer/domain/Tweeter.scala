package com.vodprofessionals.socialexplorer.domain

import _root_.java.util.Date

/**
 *
 */
case class Tweeter(id: Option[Long],
                   screenName: String,
                   joinDate: Date,
                   location: String)
