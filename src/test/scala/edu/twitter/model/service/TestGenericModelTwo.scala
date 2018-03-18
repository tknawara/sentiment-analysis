package edu.twitter.model.service

import edu.twitter.model.api.GenericModel

class TestGenericModelTwo extends GenericModel {
  override def name: String = TestGenericModelTwo.name

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Double = TestGenericModelTwo.fixedLabel
}

object TestGenericModelTwo {
  val name = "TestGenericModelTwo"
  val fixedLabel = 1.0
}
