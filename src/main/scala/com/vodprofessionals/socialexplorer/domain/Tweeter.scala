package com.vodprofessionals.socialexplorer.domain

import _root_.java.util.Date

/**
 *
 */
case class Tweeter(id: Long,
                   screenName: String,
                   joinDate: Date,
                   location: String,
                   followersCount: Long)
