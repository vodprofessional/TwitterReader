package com.vodprofessionals.socialexplorer.collector

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.RawTweet
import hu.lazycat.scala.config.Configurable
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.core.{Constants, Client}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import scala.collection.JavaConverters._



/**
 *
 */
class TwitterCollector (
      val consumerKey:    String,
      val consumerSecret: String,
      val tokenKey:       String,
      val tokenSecret:    String,
      val processor:      RawTweet => Boolean
                       ) extends Collector with LazyLogging with Configurable {

  val endpoint = new StatusesFilterEndpoint
  val twitter = (new ClientBuilder)
    .hosts(Constants.STREAM_HOST)
    .endpoint(endpoint)
    .authentication(new OAuth1(consumerKey, consumerSecret, tokenKey, tokenSecret))
    .processor(new StringDelimitedProcessor(null) {

      override def process: Boolean =
        processor(
          RawTweet(
            Stream.cons(processNextMessage, Stream.continually(processNextMessage)).dropWhile(_ == null).head
          )
        )

    })
    .build


  /**
   *
   * @param terms
   */
  override def start(terms: List[String]) = {
    endpoint.trackTerms(terms.asJava)
    twitter.connect
  }

  /**
   * Shuts down the worker application
   *
   */
  override def stop =
    twitter.stop()
}
