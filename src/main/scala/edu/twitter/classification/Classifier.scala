package edu.twitter.classification

import java.text.SimpleDateFormat
import java.util.Date

import edu.twitter.model.{GradientBoostingModel, SentimentModelDataCreator, TweetsLoader}
import edu.twitter.streaming.TwitterStream
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/** Representation of the classified tweet, we may add
  * more fields to it later. */
case class ClassifiedTweet(label: Double, tweetText: String, date: String)

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
    // @tarek-nawara Only checking for url patterns, mentions, hashtags and retweets
    // should add more validation criteria in the future see issue #13

    val tweets = new TwitterStream(ssc).createStream()
    val model = createModel()
    val hashingTF = new HashingTF(2000)
    val supportedLangIso = Set("en", "eng")
    val notValidTokens = Set("http", "@", "rt", "#", "RT")
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang)
      date = dateFormat.format(new Date())
      validTokens = tweet.getText.split(" ").filter(!notValidTokens(_))
      features = hashingTF.transform(validTokens)
      label = model(features)
    } yield ClassifiedTweet(label, tweet.getText, date)

    classifiedStream
  }


  /**
    * Create the Classification model.
    *
    * @return `GenericModel` which is a function to
    *         label the tweets.
    */
  private def createModel(): GradientBoostingModel#GenericModel = {
    val tweetsLoader = new TweetsLoader(ssc.sparkContext)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    val (trainingSet, testingSet) = twitterData.getTrainingAndTestingData()
    val gradientBoostingModel = new GradientBoostingModel(trainingSet, testingSet)
    gradientBoostingModel.createModel()
  }

}
