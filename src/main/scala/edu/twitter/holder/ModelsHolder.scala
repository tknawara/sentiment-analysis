package edu.twitter.holder

import edu.twitter.model.api.GenericModel

import scala.collection.GenSeq

trait ModelsHolder {
  def allModels(): GenSeq[GenericModel]
  def allModelNames(): List[String]
}
