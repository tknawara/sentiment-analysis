package edu.twitter.model

import org.apache.spark.sql.DataFrame
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Class Resposible on parse json files.
  *
  * @param sparkConfMaster spark configuration master
  * @param sparkConfAppName spark configuration app name
  */
class JsonParser(val sparkConfMaster: String, val sparkConfAppName: String){

  /**
    * parse Json files. load the data into Spark. Spark makes it easy to load JSON-formatted data into a dataframe
    * @param path path to json files
    * @return DataFrame represent json objects
    */
  def parse(path: String): DataFrame = {
    val conf = new SparkConf().setMaster(sparkConfMaster).setAppName(sparkConfAppName)
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    sqlContext.read.json(path)
  }
}