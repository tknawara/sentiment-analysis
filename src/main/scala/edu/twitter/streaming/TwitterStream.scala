package edu.twitter.streaming

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.Status

import scala.io.Source

/** Class Responsible for creating twitter stream given
  * a streaming context
  *
  * @param ssc streaming context for creating the stream
  *            of tweets
  */
class TwitterStream(ssc: StreamingContext) {

  /**
    * Create a tweet stream
    *
    * @param filters special filters to apply over
    *                the stream
    * @return tweets stream
    */
  def createStream(filters: Seq[String] = Nil): ReceiverInputDStream[Status] = {
    loadTwitterKeys()
    TwitterUtils.createStream(ssc, None, filters, StorageLevel.MEMORY_ONLY_SER_2)
  }

  private def loadTwitterKeys(): Unit = {
    val authPath = getClass.getClassLoader.getResource("twitter4j.properties").getPath
    val lines = Source.fromFile(authPath).getLines()
    val props = lines.map(_.split("=")).map { case Array(k, v) => (k, v) }
    props.foreach { case (k, v) => System.setProperty(k, v) }
  }
}
