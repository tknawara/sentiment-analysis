package edu.twitter.model

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
  * Loader loads all tweets model data this data is static data in FS not HDFS.
  */
class TweetsLoader {
  private val tweetsJsonParser = new JsonParser("local[*]", "Twitter");

  /**
    * Get Tweets data set as RDD.
    * @return RDD contains stream of tweets
    */
  def getTweetsDataSet(): RDD[Row] = {

    var tweetDF = tweetsJsonParser.parse("tweets\\*")
    var messages = tweetDF.select("msg")
    println("Total messages: " + messages.count())
    var (happyMessages, unhappyMessages, smallest) = cleanRecords(messages)

    //Create a dataset with equal parts happy and unhappy messages
    happyMessages.limit(smallest).unionAll(unhappyMessages.limit(smallest)).rdd
  }

  /**
    * Get only tweets that contains happy and sad words and use the presence of these words as our labels.
    * This isn’t perfect: a few sentences like “I’m not happy” will end up being incorrectly labeled as happy.
    * If you wanted more accurate labeled data, you could use a part of speech tagger like Stanford NLP or SyntaxNet.
    * @param messages All tweets text fields
    * @return tweets that contain happy word , tweets contains sad word
    */
  private def cleanRecords(messages: DataFrame) : (Dataset[Row], Dataset[Row], Integer) = {
    println("Total messages: " + messages.count())
    var happyMessages = messages.filter(messages("msg").contains("happy"))
    val countHappy = happyMessages.count()
    println("Number of happy messages: " +  countHappy)

    var unhappyMessages = messages.filter(messages("msg").contains(" sad"))
    val countUnhappy = unhappyMessages.count()
    println("Unhappy Messages: " + countUnhappy)
    (happyMessages, unhappyMessages, Math.min(countHappy, countUnhappy).toInt)
  }
}
