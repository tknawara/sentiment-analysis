package edu.twitter.model.api

import edu.twitter.model.client.dto.Label

/**
  * A generic interface for sentiment analysis model.
  */
trait GenericModel {

  def name: String

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  def getLabel(tweetText: String): Label

}
