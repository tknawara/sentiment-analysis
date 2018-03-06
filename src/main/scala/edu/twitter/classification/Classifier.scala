package edu.twitter.classification

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorSystem
import akka.stream.Materializer
import edu.twitter.model.ModelClient
import edu.twitter.model.api.GenericModelBuilder
import edu.twitter.streaming.TwitterStream
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.concurrent.ExecutionContext

/** Representation of the classified tweet, we may add
  * more fields to it later. */
case class ClassifiedTweet(label: Double, tweetText: String, date: String)

/** Responsible for building `TweeterStream` and the `Classification Model`
  * and classifying the stream with the model.
  *
  * @param ssc StreamingContext, used for model and
  *            stream creation
  */
class Classifier(ssc: StreamingContext)
                (implicit val executionContext: ExecutionContext,
                 implicit val system: ActorSystem,
                 implicit val materializer: Materializer) {

  /**
    * Build the `Classification Model` and a `TweeterStream`
    * and return a stream of `ClassifiedTweets`.
    *
    * @param genericModelBuilder the instance holding the recipe for
    *                            building the model.
    * @return stream of `ClassifiedTweets`
    */
  def createClassifiedStream(genericModelBuilder: GenericModelBuilder): DStream[ClassifiedTweet] = {
    // Only checking for url patterns, mentions, hashtags and retweets
    // should add more validation criteria in the future see issue #13

    val tweets = new TwitterStream(ssc).createStream()
    val supportedLangIso = Set("en", "eng")
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang)
      date = dateFormat.format(new Date())
      resp = ModelClient.getLabel(tweet.getText)
      if resp.isPresent
    } yield ClassifiedTweet(resp.get().getLabel, tweet.getText, date)

    classifiedStream
  }
}
