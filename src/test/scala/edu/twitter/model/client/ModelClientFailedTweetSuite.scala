package edu.twitter.model.client

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.classification.ClassificationClient
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.impl.neuralnetwork.{NeuralNetworkBuilder, NeuralNetworkModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}
import org.scalatest.junit.JUnitRunner

import scala.collection.GenSeq

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelClientFailedTweetSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig

  private class InnerModelsHolder extends ModelsHolder {

    private val gradientBoosting = () => new GradientBoostingBuilder(sc)
      .build(appConfig.paths.trainingDataPath,
        appConfig.paths.savedGradientBoostingModelPath,
        GradientBoostingModel.name)

    private val neuralNetwork = () => new NeuralNetworkBuilder(sc)
      .build(appConfig.paths.trainingDataPath,
        appConfig.paths.savedNeuralNetworkModelPath,
        NeuralNetworkModel.name)

    lazy val allModels: GenSeq[GenericModel] = List(gradientBoosting, neuralNetwork).par.map(_.apply())
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
    val resOne = ClassificationClient.callModelService(
      appConfig.modelServicePorts(NeuralNetworkModel.name), NeuralNetworkModel.name, text)
    assert(resOne.get == Label.HAPPY)

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
