package edu.twitter.model.impl.gradientboosting

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite, Ignore}

import scala.util.Try

@Ignore
@RunWith(classOf[JUnitRunner])
class ModelSuite extends FunSuite with BeforeAndAfterAll {
  @transient private var sc: SparkContext = _

  override def beforeAll(): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ModelSuite")
      .set("spark.driver.allowMultipleContexts", "true")
    sc = new SparkContext(conf)
  }

  test("model smoke test") {
    assert(Try(new GradientBoostingBuilder(sc).build()).isSuccess)
  }

  override def afterAll(): Unit = {
    if (sc != null) sc.stop()
  }
}