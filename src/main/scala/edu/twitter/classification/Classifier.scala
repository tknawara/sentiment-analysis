package edu.twitter.classification

import java.text.SimpleDateFormat
import java.util.Date

import edu.twitter.model.client.ModelClient
import edu.twitter.streaming.TwitterStream
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
    val tweets = new TwitterStream(ssc).createStream()
    val supportedLangIso = Set("en", "eng")
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang)
      date = dateFormat.format(new Date())
      resOption = ModelClient.callModel(tweet.getText)
      if resOption.isPresent
      res = resOption.get
    } yield ClassifiedTweet(res.getLabel, tweet.getText, date)

    classifiedStream
  }
}
