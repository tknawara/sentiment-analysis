package edu.twitter.model.impl.gradientboosting

import edu.twitter.config.AppConfig
import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.TweetTextFilter
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel

/**
  * Wrapper for GradientBoosting Model
  *
  * @param model actual model
  */
class GradientBoostingModel(model: GradientBoostedTreesModel, val name: String)
                           (implicit appConfig: AppConfig) extends GenericModel {
  private val hashingTF = new HashingTF(appConfig.bagOfWordsSize)

  override def getLabel(tweetText: String): Label = {
    val tokens = TweetTextFilter.filterTweet(tweetText).split(" ")
    val features = hashingTF.transform(tokens)
    val prediction = model.predict(features)
    if (prediction == 0) Label.SAD else Label.HAPPY
  }
}

/** Companion object for the model
  * only holding the name. */
object GradientBoostingModel {
  val name = "GradientBoosting"
}
