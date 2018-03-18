package edu.twitter.model.service

import edu.twitter.model.api.GenericModel

class TestGenericModel(val fixedLabel: Double, val name: String) extends GenericModel {
  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Double = fixedLabel
}