package edu.twitter.model.evaluation

import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ModelEvaluatorTest extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _
  @transient private var modelService: ModelService = _

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelEvaluatorTest")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
    modelService = new ModelService(List(new GradientBoostingBuilder(sc)))
    modelService.start()
  }

  test("model evaluator smoke test") {
    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val modelEvaluator = new ModelEvaluator(sc)
    assert(Try(modelEvaluator.evaluate(GradientBoostingModel.name, dataPath)).isSuccess)
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
    if (modelService != null) modelService.stop()
  }
}
