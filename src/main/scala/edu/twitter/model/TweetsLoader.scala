package edu.twitter.model

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}

/**
  * Loader loads all tweets model data this data is static data in FS not HDFS.
  */
class TweetsLoader(sc: SparkContext) {
  private val tweetsJsonParser = new JsonParser(sc)

  /**
    * Get Tweets data set as RDD.
    *
    * @return RDD contains stream of tweets
    */
  def getTweetsDataSet(): RDD[Row] = {
    val dataPath = this.getClass.getClassLoader.getResource("tweets").getPath

    var tweetDF = tweetsJsonParser.parse(dataPath)
    var messages = tweetDF.select("msg")
    println("Total messages: " + messages.count())
    cleanRecords(messages)
  }

  /**
    * Get only tweets that contains happy and sad words and use the presence of these words as our labels.
    * This isn’t perfect: a few sentences like “I’m not happy” will end up being incorrectly labeled as happy.
    * If you wanted more accurate labeled data, you could use a part of speech tagger like Stanford NLP or SyntaxNet.
    *
    * @param messages All tweets text fields
    * @return tweets that contain happy word , tweets contains sad word
    */
  private def cleanRecords(messages: DataFrame) : RDD[Row] = {
    var happyMessages = messages.filter(messages("msg").contains("happy"))
    val countHappy = happyMessages.count()
    println("Number of happy messages: " +  countHappy)

    var unhappyMessages = messages.filter(messages("msg").contains(" sad"))
    val countUnhappy = unhappyMessages.count()
    println("Unhappy Messages: " + countUnhappy)
    val smallest = Math.min(countHappy, countUnhappy).toInt
    //Create a dataset with equal parts happy and unhappy messages
    happyMessages.limit(smallest).union(unhappyMessages.limit(smallest)).rdd
  }
}
