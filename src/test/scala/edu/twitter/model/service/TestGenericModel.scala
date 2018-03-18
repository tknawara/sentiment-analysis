package edu.twitter.model.service

import edu.twitter.model.api.GenericModel

class TestGenericModel extends GenericModel {
  override def name: String = TestGenericModel.name

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Double = TestGenericModel.fixedLabel
}

object TestGenericModel {
  val name = "TestGenericModel"
  val fixedLabel = 0.0
}