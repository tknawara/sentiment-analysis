package edu.twitter

import edu.twitter.classification.Classifier
import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark
import edu.twitter.index.IndexHandler

/**
  * Application's entry point.
  *
  * This object will create stream of `ClassifiedTweets`
  * and persist them to elastic search, we can use
  * `kibana` to visualize the sentiment results.
  *
  */
object SentimentAnalyzer extends App {


  val indexHandler = new IndexHandler
  val indexCreationResult = indexHandler.create("twitter", "sentiment")

  indexCreationResult match {
    case Left(_) => System.exit(0)
    case Right(indexName) => runSentimentAnalyzer(indexName)
  }


  private def runSentimentAnalyzer(indexName: String): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    conf.set("es.index.auto.create", "true")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    val modelService = new ModelService(List(new GradientBoostingBuilder(sc)))
    modelService.start()


    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream(GradientBoostingModel.name)
    classifiedStream.foreachRDD(EsSpark.saveToEs(_, indexName))

    ssc.start()
    ssc.awaitTermination()

    if (ssc != null) ssc.stop(true)
    if (sc != null) sc.stop()
    modelService.stop()
  }

}
