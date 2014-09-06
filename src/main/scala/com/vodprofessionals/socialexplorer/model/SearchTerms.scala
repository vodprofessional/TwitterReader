package com.vodprofessionals.socialexplorer.model

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.config.Configurable
import scala.collection.immutable.List


/**
 *
 */
object SearchTerms extends Configurable with LazyLogging {
  var terms: List[String] = List()
  var termsChangeCallbacks: List[List[String] => Unit] = List()


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
   * Add a list of terms to the search terms list
   *
   * @param termList
   * @return
   */
  def addTerms(termList: List[String]) = {
    terms = termList ++ terms
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
