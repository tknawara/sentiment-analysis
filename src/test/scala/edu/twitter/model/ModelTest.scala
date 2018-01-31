package edu.twitter.model

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ramadan on 1/23/2018.
  */
object ModelTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    var tweetsLoader = new TweetsLoader(sc)
    var twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    var (trainingSet , testingSet) = twitterData.getTrainingAndTestingData()
    var gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    gradientBoostingModel.createModel()
  }
}
