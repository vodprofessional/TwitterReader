package com.vodprofessionals.socialexplorer.processor

/**
 *
 */
abstract class Processor {
  /**
   *
   */
  def start(): Unit = {}

  /**
   *
   * @param message
   */
  def process(message: String): Unit

  /**
   *
   */
  def stop(): Unit = {}
}
