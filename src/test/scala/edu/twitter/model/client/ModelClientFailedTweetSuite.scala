package edu.twitter.model.client

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.classification.ClassificationClient
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.gradientboosting.normal.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.impl.neuralnetwork.normal.{NeuralNetworkBuilder, NeuralNetworkModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

import scala.collection.GenSeq

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelClientFailedTweetSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig

  private class InnerModelsHolder extends ModelsHolder {
    lazy val allModels: GenSeq[GenericModel] = List(new GradientBoostingBuilder(sc), new NeuralNetworkBuilder(sc)).par.map(_.build())
    lazy val allModelNames: List[String] = List(GradientBoostingModel.name, NeuralNetworkModel.name)
  }

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelEvaluatorTest")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
    val models = new InnerModelsHolder
    modelService = new ModelService(models)
    modelService.start()
  }

  test("test jackson") {
    val text = "RT @ChildhoodShows: Before there was Troy Bolton there was Eddie Thomas https://t.co/QlxfSvtojc"
    (1 to 1000).par.foreach { _ =>
      val resOne = ClassificationClient.callModelService(
        appConfig.modelServicePorts(NeuralNetworkModel.name), NeuralNetworkModel.name, text)
      assert(resOne.get == Label.SAD)
    }

    val resTwo = ClassificationClient.callModelService(
      appConfig.modelServicePorts(GradientBoostingModel.name), GradientBoostingModel.name, text)
    assert(resTwo.nonEmpty)
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
    if (modelService != null) {
      modelService.stop()
    }
  }
}
