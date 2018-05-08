package edu.twitter.model.impl.gradientboosting.normal

import edu.twitter.config.AppConfig
import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.TweetTextFilter
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel

/** Wrapper for GradientBoosting Model
  *
  * @param model     actual model
  * @param appConfig application configuration
  */
class GradientBoostingModel(model: GradientBoostedTreesModel)(implicit appConfig: AppConfig) extends GenericModel {
  val name: String = GradientBoostingModel.name
  private val hashingTF = new HashingTF(appConfig.bagOfWordsSize)

  /** @inheritdoc */
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
