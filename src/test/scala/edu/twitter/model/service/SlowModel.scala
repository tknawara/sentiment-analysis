package edu.twitter.model.service

import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label

class SlowModel(sleepInterval: Long) extends GenericModel {
  override def name: String = SlowModel.name

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Label = {
    Thread.sleep(sleepInterval)
    Label.HAPPY
  }
}

object SlowModel {
  val name: String = "SlowModel"
}
