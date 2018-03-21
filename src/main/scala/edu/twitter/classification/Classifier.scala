package edu.twitter.classification

import java.text.SimpleDateFormat

import edu.twitter.model.client.ModelClient
import edu.twitter.streaming.TwitterStream
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/** Representation of the classified tweet, we may add
  * more fields to it later. */
case class ClassifiedTweet(label: Double, tweetText: String, date: String, location: String, modelName: String)

/** Responsible for building `TweeterStream` and the `Classification Model`
  * and classifying the stream with the model.
  *
  * @param ssc StreamingContext, used for model and
  *            stream creation
  */
class Classifier(ssc: StreamingContext) {
  private lazy val tweets = new TwitterStream(ssc).createStream()

  /**
    * Build the `Classification Model` and a `TweeterStream`
    * and return a stream of `ClassifiedTweets`.
    *
    * @param modelName name of target model.
    * @return stream of `ClassifiedTweets`
    */
  def createClassifiedStream(modelName: String): DStream[ClassifiedTweet] = {
    val supportedLangIso = Set("en", "eng")
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang)
      resOption = ModelClient.callModelService(modelName, tweet.getText)
      if resOption.isPresent
      res = resOption.get
      label = res.getKibanaRepresentation
      date = dateFormat.format(tweet.getCreatedAt)
      location = if (tweet.getGeoLocation != null) s"${tweet.getGeoLocation.getLatitude},${tweet.getGeoLocation.getLongitude}" else null
    } yield ClassifiedTweet(label, tweet.getText, date, location, modelName)

    classifiedStream
  }


}