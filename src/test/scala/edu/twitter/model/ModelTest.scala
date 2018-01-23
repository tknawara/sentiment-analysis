package edu.twitter.model

/**
  * Created by Ramadan on 1/23/2018.
  */
object ModelTest {
  def main(args: Array[String]): Unit = {
    var tweetsLoader = new TweetsLoader()
    var twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    var (trainingSet , testingSet) = twitterData.getTrainingAndTestingData()
    var gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    gradientBoostingModel.getModel()
  }
}
