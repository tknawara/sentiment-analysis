package edu.twitter.model.service

import edu.twitter.model.client.ModelClient
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelClientServiceSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _

  override def beforeAll(): Unit = {
    modelService = new ModelService(List(new TestGenericModelBuilder()))
    modelService.start()
  }

  test("the model client's result should match the model's response") {
    val resp = ModelClient.callModelService(TestGenericModel.name, "hello")
    assert(resp.get().getLabel == TestGenericModel.fixedLabel)
  }

  override def afterAll(): Unit = {
    if (modelService != null) {
      modelService.stop()
    }
  }
}
