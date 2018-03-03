package edu.twitter.model

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ramadan on 1/23/2018.
  */
object ModelTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    new GradientBoostingBuilder(sc).build()
  }
}
