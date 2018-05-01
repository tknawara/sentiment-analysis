package edu.twitter.model.evaluation

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.impl.TestModelsHolder
import edu.twitter.model.impl.gradientboosting.GradientBoostingModel
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

import scala.util.Try

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelEvaluatorSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _
  @transient private var modelService: ModelService = _
  implicit val appConfig: AppConfig = DevConfig

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelEvaluatorTest")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
    val models = new TestModelsHolder
    modelService = new ModelService(models)
    modelService.start()
  }

  test("model evaluator smoke test") {
    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val modelEvaluator = new ModelEvaluator(sc)
    assert(Try(modelEvaluator.evaluate(GradientBoostingModel.name)).isSuccess)
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
    if (modelService != null) modelService.stop()
  }
}
