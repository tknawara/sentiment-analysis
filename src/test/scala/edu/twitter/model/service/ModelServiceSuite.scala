package edu.twitter.model.service

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.TestModelsHolder
import edu.twitter.model.client.classification.ClassificationClient
import edu.twitter.model.client.dto.Label
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelServiceSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig
  private val builderOne = new TestGenericModelBuilder(Label.SAD, "ModelOne")
  private val builderTwo = new TestGenericModelBuilder(Label.HAPPY, "ModelTwo")
  private val slowBuilder = new SlowModelBuilder(10000)

  override def beforeAll(): Unit = {
    val models = new TestModelsHolder
    modelService = new ModelService(models)
    modelService.start()
  }

  test("the model client's result should match the model's response") {
    val resp = ClassificationClient.callModelService("8080", builderOne.modelName, "hello")
    assert(resp.get == builderOne.fixedLabel)
  }

  test("Model service can support multiple models") {
    val tweet = "hello"
    val port = "8080"
    val modelOneClassification = ClassificationClient.callModelService(port, builderOne.modelName, tweet)
    val modelTwoClassification = ClassificationClient.callModelService(port, builderTwo.modelName, tweet)
    assert(modelOneClassification.get == builderOne.fixedLabel)
    assert(modelTwoClassification.get == builderTwo.fixedLabel)
  }

  test("Client should timeout in case of very slow model") {
    val start = System.currentTimeMillis()
    val resp = ClassificationClient.callModelService("8080", SlowModel.name, "hello")
    val elapsedTime = System.currentTimeMillis() - start
    assert(resp.isEmpty)
    assert(elapsedTime < 10000)
  }

  override def afterAll(): Unit = {
    if (modelService != null) {
      modelService.stop()
    }
  }
}
