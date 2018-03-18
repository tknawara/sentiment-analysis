package edu.twitter.model.service

import edu.twitter.model.client.ModelClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

@RunWith(classOf[JUnitRunner])
class MultipleModels extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _

  override def beforeAll(): Unit = {
    modelService = new ModelService(List(new TestGenericModelBuilder, new TestGenericModelBuilderTwo))
    modelService.start()
  }

  test("Model service can support multiple models") {
    val tweet = "hello"
    val modelOneClassification = ModelClient.callModelService(TestGenericModel.name, tweet)
    val modelTwoClassification = ModelClient.callModelService(TestGenericModelTwo.name, tweet)
    assert(modelOneClassification.get().getLabel == TestGenericModel.fixedLabel)
    assert(modelTwoClassification.get().getLabel == TestGenericModelTwo.fixedLabel)
  }

  override def afterAll(): Unit = {
    if (modelService != null) {
      modelService.stop()
    }
  }
}
