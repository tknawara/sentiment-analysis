package edu.twitter.classification

import java.text.SimpleDateFormat

import edu.twitter.config.AppConfig
import edu.twitter.model.client.classification.ClassificationClient
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
  /**
    * Build the `Classification Model` and a `TweeterStream`
    * and return a stream of `ClassifiedTweets`.
    *
    * @param models seq of model names.
    * @return stream of `ClassifiedTweets`
    */
  def createClassifiedStream(models: Seq[String])(implicit appConfig: AppConfig): DStream[ClassifiedTweet] = {
    val tweets = new TwitterStream(ssc).createStream()
    val supportedLangIso = Set("en", "eng")
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val ports = models.map(appConfig.modelServicePorts)
    val classifiedStream = for {
      tweet <- tweets
      if supportedLangIso(tweet.getLang) && !tweet.getText.startsWith("RT ")
      responses = models.zip(ports).map { case (name, port) =>
        name -> ClassificationClient.callModelService(port, name, tweet.getText)
      }
      allExist = responses.forall { case (_, r) => r.nonEmpty }
      if allExist
      (modelName, modelResp) <- responses
      callRes <- modelResp
      label = callRes.getKibanaRepresentation
      date = dateFormat.format(tweet.getCreatedAt)
      geo = tweet.getGeoLocation
      location = if (geo != null) s"${geo.getLatitude},${geo.getLongitude}" else null
    } yield ClassifiedTweet(label, tweet.getText, date, location, modelName)

    classifiedStream
  }

}