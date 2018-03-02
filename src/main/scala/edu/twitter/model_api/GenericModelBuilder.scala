package edu.twitter.model_api

/**
  * A generic interface for the object responsable for creating any model.
  */
trait GenericModelBuilder {
  def build(): GenericModel
}
