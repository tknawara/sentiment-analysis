package edu.twitter.model.service

import edu.twitter.model.api.{GenericModel, GenericModelBuilder}

class SlowModelBuilder(sleepInterval: Long) extends GenericModelBuilder {
  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  override def build(): GenericModel = new SlowModel(sleepInterval)
}
