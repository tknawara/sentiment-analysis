package edu.twitter.model

import edu.twitter.model_api.GenericModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel

/**
  * Wrapper for GradientBoosting Model
  *
  * @param model actual model
  */
@SerialVersionUID(1L)
class GradientBoostingModel(model: GradientBoostedTreesModel) extends GenericModel with Serializable {

  val hashingTF = new HashingTF(2000)
  val invalidTokens = Set("http", "@", "rt", "#", "RT")

  override def getLabel(tweetText: String): Double = {
    val tokens = tweetText.split(" ").filter(!invalidTokens(_))
    val features = hashingTF.transform(tweetText.split(" "))
    model.predict(features)
  }
}
