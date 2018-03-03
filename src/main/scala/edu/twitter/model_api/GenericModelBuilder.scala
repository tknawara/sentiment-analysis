package edu.twitter.model_api

/**
  * A generic interface for the object responsable for creating any model.
  */
trait GenericModelBuilder {
  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  def build(): GenericModel
}
