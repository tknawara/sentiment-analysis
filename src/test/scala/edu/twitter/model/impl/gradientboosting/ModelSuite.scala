package edu.twitter.model.impl.gradientboosting

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ModelSuite extends FunSuite {
  test("model smoke test") {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    assert(Try(new GradientBoostingBuilder(sc).build()).isSuccess)
  }
}