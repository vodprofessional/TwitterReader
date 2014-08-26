package com.vodprofessionals.socialexplorer.collector

object CollectorMessages {
  sealed trait CollectorMessage

  case class StartTwitterCollector(terms: List[String])
    extends CollectorMessage
  case class RestartTwitterCollector(terms: List[String])
  case object StopTwitterCollector extends CollectorMessage
}

/**
 *
 */
abstract class Collector {
  /**
   *
   * @param terms
   */
  def start(terms: List[String])

  /**
   *
   */
  def stop
}
