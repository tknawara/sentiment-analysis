package edu.twitter.model.service

import edu.twitter.model.client.ModelClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

@RunWith(classOf[JUnitRunner])
class ModelClientServiceSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _
  private val builder = new TestGenericModelBuilder(0, "ModelOne")

  override def beforeAll(): Unit = {
    modelService = new ModelService(List(builder))
    modelService.start()
  }

  test("the model client's result should match the model's response") {
    val resp = ModelClient.callModelService(builder.modelName, "hello")
    assert(resp.get().getLabel == builder.fixedLabel)
  }

  override def afterAll(): Unit = {
    if (modelService != null) {
      modelService.stop()
    }
  }
}
