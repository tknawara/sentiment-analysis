package edu.twitter.model.evaluation

import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ModelEvaluatorTest extends FunSuite {
  test("model evaluator smoke test") {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val modelService = new ModelService(new GradientBoostingBuilder(sc))
    modelService.start()

    val modelEvaluator = new ModelEvaluator(sc)
    assert(Try(modelEvaluator.evaluate(GradientBoostingModel.name, dataPath)).isSuccess)
  }
}
