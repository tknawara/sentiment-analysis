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
    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val tweetDF = tweetsJsonParser.parse(dataPath)
    equalizeDataSet(tweetDF)
  }

  /**
    * Equalize the number of happy and sad tweets in the data set to
    * avoid any overfitting.
    *
    * @param messages All tweets text fields
    * @return tweets that contain happy word , tweets contains sad word
    */
  private def equalizeDataSet(messages: DataFrame): RDD[Row] = {
    val happyMessages = messages.filter(messages("label").contains("1.0"))
    val countHappy = happyMessages.count()
    println("Number of happy messages: " + countHappy)

    val unhappyMessages = messages.filter(messages("label").contains("0.0"))
    val countUnhappy = unhappyMessages.count()
    println("Unhappy Messages: " + countUnhappy)
    val smallest = Math.min(countHappy, countUnhappy).toInt
    happyMessages.limit(smallest).union(unhappyMessages.limit(smallest)).rdd
  }
}
