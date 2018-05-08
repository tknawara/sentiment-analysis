package edu.twitter.model.evaluation

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.holder.impl.Models
import edu.twitter.model.impl.gradientboosting.normal.GradientBoostingModel
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
  @transient private var modelHolder: ModelsHolder = _
  implicit val appConfig: AppConfig = DevConfig

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelEvaluatorTest")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
    modelHolder = new Models(sc)
    modelService = new ModelService(modelHolder)
    modelService.start()
  }

  test("model evaluator smoke test") {
    val modelEvaluator = new ModelEvaluator(sc)
    assert(Try(modelEvaluator.evaluate(GradientBoostingModel.name)).isSuccess)
  }

  test("model evaluator spelling correctness evaluation test") {
    val modelEvaluator = new ModelEvaluator(sc)
    modelEvaluator.evaluateWithCorrection(modelHolder.allModelNames())
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
    if (modelService != null) modelService.stop()
  }
}
