package com.vodprofessionals.socialexplorer.model

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.config.Configurable
import scala.collection.immutable.List


/**
 *
 */
object SearchTerms extends Configurable with LazyLogging {
  var terms: Set[String] = Set()
  var dirtyTerms: Set[String] = Set()
  var termsChangeCallbacks: Set[Set[String] => Unit] = Set()
  var isDirty = false


  /**
   * Commits the dirty terms to the active terms
   *
   * @return
   */
  def commitDirty = {
    if (isDirty) {
      terms = dirtyTerms
      isDirty = false
      for {callback <- termsChangeCallbacks} yield callback(terms)
    }
  }

  /**
   * Add a term to search for in the input
   *
   * @param term
   */
  def addTerm(term: String) = {
    addTerms(Set(term))
  }

  /**
   * Add a list of terms to the search terms list
   *
   * @param termList
   * @return
   */
  def addTerms(termList: Set[String]) = {
    if (!isDirty) {
      dirtyTerms = terms.toSet[String]
    }
    isDirty = true

    dirtyTerms = dirtyTerms ++ termList
  }

  /**
   * Remove a term from the search terms
   *
   * @param term
   */
  def removeTerm(term: String) = {
    removeTerms(Set(term))
  }

  /**
   * Remove terms from the actual list of terms to search for
   *
   * @param terms
   * @return
   */
  def removeTerms(terms: Set[String]) = {
    if (!isDirty) {
      dirtyTerms = terms.toSet[String]
    }
    isDirty = true

    dirtyTerms = dirtyTerms diff terms
  }

  /**
   * Return the current set of terms
   *
   * @return
   */
  def getActiveTerms = terms

  /**
   *
   * @return
   */
  def getTermsCount =
    if (isDirty)
      dirtyTerms.size
    else
      terms.size

  /**
   * Reset the terms list and replace it with the new list of terms
   *
   * @param t
   */
  def replaceTerms(t: Set[String]): Unit = {
    isDirty = true
    dirtyTerms = t
  }

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
   * Add a callback when the terms change
   *
   * @param callback
   */
  def addCallback(callback: Set[String] => Unit) = {
    termsChangeCallbacks = termsChangeCallbacks + callback
  }
}
