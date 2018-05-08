package edu.twitter.model.service

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.classification.ClassificationClient
import edu.twitter.model.client.dto.Label
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

import scala.collection.GenSeq

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelServiceSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig
  private val modelOne = new TestGenericModel(Label.SAD, "ModelOne")
  private val modelTwo = new TestGenericModel(Label.HAPPY, "ModelTwo")
  private val slowModel = new SlowModel(10000)

  private class InnerModelsHolder extends ModelsHolder {
    val allModels: GenSeq[GenericModel] = List(modelOne, modelTwo, slowModel)
    val allModelNames: List[String] = List(modelOne.name, modelTwo.name, slowModel.name)
  }

  override def beforeAll(): Unit = {
    val models = new InnerModelsHolder
    modelService = new ModelService(models)
    modelService.start()
  }

  test("the model client's result should match the model's response") {
    val resp = ClassificationClient.callModelService("8080", modelOne.name, "hello")
    assert(resp.get == modelOne.fixedLabel)
  }

  test("Model service can support multiple models") {
    val tweet = "hello"
    val port = "8080"
    val modelOneClassification = ClassificationClient.callModelService(port, modelOne.name, tweet)
    val modelTwoClassification = ClassificationClient.callModelService(port, modelTwo.name, tweet)
    assert(modelOneClassification.get == modelOne.fixedLabel)
    assert(modelTwoClassification.get == modelTwo.fixedLabel)
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
