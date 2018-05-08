package edu.twitter.model.impl.gradientboosting.correct

import edu.twitter.config.AppConfig
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.gradientboosting.normal.GradientBoostingModel
import edu.twitter.service.SpellingCorrectionService
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel

class GradientBoostingCorrectSpellingModel(model: GradientBoostedTreesModel)(implicit appConfig: AppConfig)
  extends GradientBoostingModel(model) {

  override val name: String = GradientBoostingCorrectSpellingModel.name

  override def getLabel(tweetText: String): Label = {
    val correctTweet = SpellingCorrectionService.correctSpelling(tweetText)
    super.getLabel(correctTweet)
  }
}

/** Holder of Gradient boosting with
  * spelling correction model. */
object GradientBoostingCorrectSpellingModel {
  val name = "GradientBoostingCorrectSpellingModel"
}
