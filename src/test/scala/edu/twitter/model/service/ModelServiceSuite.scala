package edu.twitter.model.service

import edu.twitter.model.client.ModelClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelServiceSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _
  private val builderOne = new TestGenericModelBuilder(0, "ModelOne")
  private val builderTwo = new TestGenericModelBuilder(1, "ModelTwo")

  override def beforeAll(): Unit = {
    modelService = new ModelService(List(builderOne, builderTwo))
    modelService.start()
  }

  test("the model client's result should match the model's response") {
    val resp = ModelClient.callModelService(builderOne.modelName, "hello")
    assert(resp.get().getLabel == builderOne.fixedLabel)
  }

  test("Model service can support multiple models") {
    val tweet = "hello"
    val modelOneClassification = ModelClient.callModelService(builderOne.modelName, tweet)
    val modelTwoClassification = ModelClient.callModelService(builderTwo.modelName, tweet)
    assert(modelOneClassification.get().getLabel == builderOne.fixedLabel)
    assert(modelTwoClassification.get().getLabel == builderTwo.fixedLabel)
  }

  override def afterAll(): Unit = {
    if (modelService != null) {
      modelService.stop()
    }
  }
}
