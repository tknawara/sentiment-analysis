package edu.twitter.streaming

import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark

object TestElasticsearch {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Twitter").setMaster("local[*]")
    conf.set("es.index.auto.create", "true")
    val sc = new SparkContext(conf)

    val numbers = Map("one" -> 1, "two" -> 2, "three" -> 3)
    val airports = Map("arrival" -> "Otopeni", "SFO" -> "San Fran")
    val rdd = sc.makeRDD(Seq(numbers, airports))
    EsSpark.saveToEs(rdd, "spark/docs")
  }
}
