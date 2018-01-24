package edu.twitter.streaming

import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object TwitterTest {
  @transient var sc: SparkContext = _

  def main(args: Array[String]): Unit = {
    val ssc = configureStreamingContext()
    val tweets = new TwitterStream(ssc).createStream()
    Logger.getRootLogger.setLevel(Level.ERROR)
    //tweets.foreachRDD(rdd => rdd.take(10).foreach(println(_)))
    tweets.foreachRDD(rdd => rdd.take(10).map(status => status.getText).filter(str => str.contains("to")).foreach(println(_)))
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }

  def configureStreamingContext(): StreamingContext = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    sc = new SparkContext(conf)
    new StreamingContext(sc, Seconds(10))
  }

}
