package edu.twitter.holder

import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label
import edu.twitter.model.service.TestGenericModel

import scala.collection.GenSeq

class TestModelsHolder extends ModelsHolder {
  val allModels: GenSeq[GenericModel] = List(new TestGenericModel(Label.HAPPY, "ModelOne"))
  val allModelNames: List[String] = List("ModelOne")
}
