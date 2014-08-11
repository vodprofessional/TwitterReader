package com.vodprofessionals.socialexplorer.collector

/**
 *
 */
abstract class Collector {
  def start

  def process(terms: List[String])

  def stop
}
