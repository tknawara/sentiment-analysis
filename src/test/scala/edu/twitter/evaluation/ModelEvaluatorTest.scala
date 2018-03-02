package edu.twitter.evaluation

import edu.twitter.model.{GradientBoostingModel, SentimentModelDataCreator, TweetsLoader}
import org.apache.spark.{SparkConf, SparkContext}

object ModelEvaluatorTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    val tweetsLoader = new TweetsLoader(sc)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    val (trainingSet , testingSet) = twitterData.getTrainingAndTestingData()
    val gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    val model = gradientBoostingModel.createModel()
    val modelEvaluator = new ModelEvaluator(sc)
    modelEvaluator.evaluate(model)
  }
}
