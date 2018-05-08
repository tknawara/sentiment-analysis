package edu.twitter.model.impl.gradientboosting.normal

import edu.twitter.config.AppConfig
import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.impl.gradientboosting.GradientBoostingBaseBuilder
import org.apache.spark.SparkContext

class GradientBoostingBuilder(sc: SparkContext)(implicit appConfig: AppConfig)
  extends GradientBoostingBaseBuilder(sc)
    with GenericModelBuilder {

  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  override def build(): GenericModel = {
    val baseModel = super.build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedGradientBoostingModelPath,
      GradientBoostingModel.name)
    new GradientBoostingModel(baseModel)
  }
}
