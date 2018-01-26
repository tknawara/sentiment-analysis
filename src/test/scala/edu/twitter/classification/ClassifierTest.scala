package edu.twitter.classification

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object ClassifierTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream()
    classifiedStream.foreachRDD(rdd => rdd.take(10).foreach(println(_)))

    ssc.start()
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }

  }
}
