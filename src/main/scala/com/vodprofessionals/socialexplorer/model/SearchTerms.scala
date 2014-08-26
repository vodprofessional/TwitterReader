package com.vodprofessionals.socialexplorer.model

import hu.lazycat.scala.config.Configurable
import scala.collection.immutable.List
import scala.collection.JavaConverters._

/**
 *
 */
object SearchTerms extends Configurable {
  var terms: List[String] = List()
  var termsChangeCallbacks: List[List[String] => Unit] = List()

  terms = CONFIG.getStringList("temp.terms").asScala.toList


  /**
   * Add a term to search for in the input
   *
   * @param term
   */
  def addTerm(term: String) = {
    term :: terms
    for {callback <- termsChangeCallbacks} yield callback(terms)
  }

  /**
   * Remove a term from the actual list of terms to search for
   *
   * @param term
   * @return
   */
  def removeTerm(term: String) = {
    terms diff List(term)
    for {callback <- termsChangeCallbacks} yield callback(terms)
  }

  /**
   * Return the current set of terms
   *
   * @return
   */
  def getTerms = terms

  /**
    * Match the term list against the text
    *
    * @param text
    * @return
    */
  def matchTerms(text: String) = {
    val termRegex = {"(?i)("+terms.mkString("|")+")"}.r

    (for(m <- termRegex.findAllIn(text)) yield m.toLowerCase).toSet
  }

  /**
   * Add a callback when the terms chage
   *
   * @param callback
   */
  def addCallback(callback: List[String] => Unit) = {
    callback :: termsChangeCallbacks
  }
}
