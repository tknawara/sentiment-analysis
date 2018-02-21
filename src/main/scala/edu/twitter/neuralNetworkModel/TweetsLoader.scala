package edu.twitter.neuralNetworkModel

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Row}

/**
  * Loader loads all tweets model data this data is static data in FS not HDFS.
  */
class TweetsLoader(sc: SparkContext) {
  private val tweetsJsonParser = new JsonParser(sc)

  /**
    * @return DataFrame contains stream of tweets
    */
  def getTweetsDataSet(): DataFrame = {
    val dataPath = this.getClass.getClassLoader.getResource("tweets").getPath
    tweetsJsonParser.parse(dataPath)
  }
}
