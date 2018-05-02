package edu.twitter.holder.api

import edu.twitter.model.api.GenericModel

import scala.collection.GenSeq

/** Generic Interface for any
  * Models provider. */
trait ModelsHolder {

  /**
    * @return all available models
    */
  def allModels(): GenSeq[GenericModel]

  /**
    * @return all available models' names.
    */
  def allModelNames(): List[String]
}
