package com.vodprofessionals.socialexplorer.util.twitter

import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder


/**
 *
 */
class TwitterUser(
      val consumerKey:    String,
      val consumerSecret: String
                 ) {

  val builder = new ConfigurationBuilder();
  builder.setApplicationOnlyAuthEnabled(true);
  val twitter =  new TwitterFactory(builder.build()).getInstance()
  twitter.setOAuthConsumer(consumerKey, consumerSecret)
  twitter.getOAuth2Token


  /**
   *
   * @param screenNames
   */
  def lookup(screenNames: List[String]): Unit =
    twitter.lookupUsers(screenNames.toArray)

}
