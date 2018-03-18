package edu.twitter.model.service

import edu.twitter.model.client.ModelClient
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelClientServiceSuite extends FunSuite {
  test("the model client's result should match the model's response") {
    val modelService = new ModelService(new TestGenericModelBuilder())
    modelService.start()

    val modeName = "TestGenericModel"
    val tweet = "hello"
    val resp = ModelClient.callModelService(modeName, tweet)

    assert(resp.get().getLabel == TestGenericModel.fixedLabel)
  }
}
