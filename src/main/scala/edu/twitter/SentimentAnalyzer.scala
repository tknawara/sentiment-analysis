package edu.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.twitter.classification.Classifier
import edu.twitter.model.impl.GradientBoostingBuilder
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark

import scala.concurrent.ExecutionContextExecutor

/**
  * Application's entry point.
  *
  * This object will create stream of `ClassifiedTweets`
  * and persist them to elastic search, we can use
  * `kibana` to visualize the sentiment results.
  *
  */
object SentimentAnalyzer extends App {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
  conf.set("es.index.auto.create", "true")
  val sc = new SparkContext(conf)
  val ssc = new StreamingContext(sc, Seconds(10))

  val classifier = new Classifier(ssc)
  val classifiedStream = classifier.createClassifiedStream(new GradientBoostingBuilder(sc))
  classifiedStream.foreachRDD(EsSpark.saveToEs(_, "twitter/sentiment"))

  ssc.start()
  ssc.awaitTermination()
  ssc.stop(true)

  if (sc != null) {
    sc.stop()
  }
}
