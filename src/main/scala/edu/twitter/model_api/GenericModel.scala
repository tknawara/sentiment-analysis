package edu.twitter.model_api

/**
  * A generic interface for sentiment analysis model.
  */
trait GenericModel {

  /**
    * Classify the given tweet.
    *
    * @param tweetText
    * @return 0 for sad & 1 for happy
    */
  def getLabel(tweetText: String): Double
}
