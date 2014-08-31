package com.vodprofessionals.socialexplorer.domain

import _root_.java.util.Date

/**
 *
 */
case class ServiceExtra(id: Option[Long],
                        name: String,
                        txDateTime: Date,
                        channel: String,
                        serviceId: Long)
