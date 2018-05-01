package edu.twitter.model.service

import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.client.dto.Label

class TestGenericModelBuilder(val fixedLabel: Label, val modelName: String) extends GenericModelBuilder {
  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  override def build(dataPath: String, savePath: String, resultingModelName: String): GenericModel =
    new TestGenericModel(fixedLabel, modelName)
}
