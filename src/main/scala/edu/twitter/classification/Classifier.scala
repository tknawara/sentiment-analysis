package edu.twitter.classification

import edu.twitter.model.{GradientBoostingModel, SentimentModelDataCreator, TweetsLoader}
import edu.twitter.streaming.TwitterStream
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/** Representation of the classified tweet, we may add
  * more fields to it later. */
case class ClassifiedTweet(label: Double, tweetText: String)

/** Responsible for building `TweeterStream` and the `Classification Model`
  * and classifying the stream with the model.
  *
  * @param ssc StreamingContext, used for model and
  *            stream creation
  */
class Classifier(ssc: StreamingContext) {

  /**
    * Build the `Classification Model` and a `TweeterStream`
    * and return a stream of `ClassifiedTweets`.
    *
    * @return stream of `ClassifiedTweets`
    */
  def createClassifiedStream(): DStream[ClassifiedTweet] = {
    val tweets = new TwitterStream(ssc).createStream()
    val model = createModel()
    val hashingTF = new HashingTF(2000)
    val supportedLangIso = Set("en", "eng")
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang)
      features = hashingTF.transform(tweet.getText.split(" "))
      label = model(features)
    } yield ClassifiedTweet(label, tweet.getText)

    classifiedStream
  }

  /** Create the Classification model. */
  private def createModel(): GradientBoostingModel#GenericModel = {
    val tweetsLoader = new TweetsLoader(ssc.sparkContext)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    val (trainingSet, testingSet) = twitterData.getTrainingAndTestingData()
    val gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    gradientBoostingModel.createModel()
  }

}
