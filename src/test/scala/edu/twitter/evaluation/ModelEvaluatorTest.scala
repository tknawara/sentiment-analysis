package edu.twitter.evaluation

import edu.twitter.model.evaluation.ModelEvaluator
import edu.twitter.model.impl.GradientBoostingBuilder
import org.apache.spark.{SparkConf, SparkContext}

object ModelEvaluatorTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    val model = new GradientBoostingBuilder(sc).build()
    val modelEvaluator = new ModelEvaluator(sc)
    modelEvaluator.evaluate(model)
  }
}
