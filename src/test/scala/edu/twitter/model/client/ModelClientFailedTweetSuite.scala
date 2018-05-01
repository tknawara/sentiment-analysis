package edu.twitter.model.client

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.model.Label
import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.impl.neuralnetwork.{NeuralNetworkBuilder, NeuralNetworkModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}
import org.scalatest.junit.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelClientFailedTweetSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelEvaluatorTest")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
    modelService = new ModelService(List(new NeuralNetworkBuilder(sc), new GradientBoostingBuilder(sc)))
    modelService.start()
  }

  test("test jackson") {
    val text = "RT @ChildhoodShows: Before there was Troy Bolton there was Eddie Thomas https://t.co/QlxfSvtojc"
    val resOne = ModelClient.callModelService(appConfig.modelServicePorts(NeuralNetworkModel.name), NeuralNetworkModel.name, text)
    assert(resOne.get == Label.HAPPY)

    val resTwo = ModelClient.callModelService(appConfig.modelServicePorts(GradientBoostingModel.name), GradientBoostingModel.name, text)
    assert(resTwo.nonEmpty)
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
    if (modelService != null) {
      modelService.stop()
    }
  }
}