package edu.twitter.classification

import edu.twitter.model.{GradientBoostingModel, SentimentModelDataCreator, TweetsLoader}
import edu.twitter.streaming.TwitterStream
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

case class ClassifiedTweet(label: Double, tweetText: String)

class Classifier(ssc: StreamingContext) {

  def createClassifiedStream(): DStream[ClassifiedTweet] = {
    val tweets = new TwitterStream(ssc).createStream()
    val model = createModel()
    val hashingTF = new HashingTF(2000)
    val classifiedStream = for {
      tweet <- tweets
      features = hashingTF.transform(tweet.getText.split(" "))
      label = model(features)
    } yield ClassifiedTweet(label, tweet.getText)

    classifiedStream
  }

  private def createModel(): GradientBoostingModel#GenericModel = {
    val tweetsLoader = new TweetsLoader(ssc.sparkContext)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    val (trainingSet, testingSet) = twitterData.getTrainingAndTestingData()
    val gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    gradientBoostingModel.createModel()
  }

}
