package edu.twitter.model.service

import edu.twitter.model.Label
import edu.twitter.model.api.GenericModel

class TestGenericModel extends GenericModel {
  override def name: String = "TestGenericModel"

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Label = Label.HAPPY
}