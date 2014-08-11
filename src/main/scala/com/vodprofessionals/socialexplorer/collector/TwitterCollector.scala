package com.vodprofessionals.socialexplorer.collector

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinRouter
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.RawTweet
import com.vodprofessionals.socialexplorer.model.SearchTerms
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import hu.lazycat.scala.config.Configurable
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.core.{Constants, Client}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import scala.collection.JavaConverters._


class TwitterCollector extends Collector with LazyLogging with Configurable with Actor {

  var stopFlag: Boolean = false
  val QUEUE: BlockingQueue[String] = new LinkedBlockingQueue[String](10000)
  var twitter: Client = null

  def receive = {
    case _ => Unit
  }

  /**
   *
   */
  override def start = {
    // Register shutdown hook to terminate the Twitter hose
    sys addShutdownHook { stop }
    SearchTerms.addCallback(process)
    process(SearchTerms.getTerms)

    val processorCores = Runtime.getRuntime().availableProcessors() * 2
    var nrOfWorkers = 0;
    if (processorCores > 2) {
      nrOfWorkers = processorCores * 2
    } else {
      nrOfWorkers = 1
    }
    val processorActors= context.actorOf(Props[TwitterProcessor].withRouter(RoundRobinRouter(nrOfWorkers)),
                                                    name = "workerRouter")

    while(true != stopFlag) {
      while (!twitter.isDone) {
        val jsonMessage = QUEUE.take
        if (null != jsonMessage) {
          // TODO implement pushing to workers
          processorActors ! RawTweet(jsonMessage)
        }
      }
      Thread.`yield`()
    }

    twitter.stop()
  }

  /**
   *
   * @param terms
   */
  override def process(terms: List[String]) = {
    try { stop }
    val endpoint = new StatusesFilterEndpoint
    twitter = (new ClientBuilder).hosts(Constants.STREAM_HOST)
      .endpoint(endpoint)
      .authentication(new OAuth1(CONFIG.getString("twitter.consumer.key"),
      CONFIG.getString("twitter.consumer.secret"),
      CONFIG.getString("twitter.token.key"),
      CONFIG.getString("twitter.token.secret")))
      .processor(new StringDelimitedProcessor(QUEUE))
      .build
    twitter.connect
    endpoint.trackTerms(terms.asJava)
  }


  /**
   * Shuts down the worker application
   *
   */
  override def stop = stopFlag = true
}
