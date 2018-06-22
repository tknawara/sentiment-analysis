package edu.twitter

import edu.twitter.classification.Classifier
import edu.twitter.config.{AppConfig, DevConfig, ProdConfig}
import edu.twitter.holder.impl.Models
import edu.twitter.index.IndexHandler
import edu.twitter.model.evaluation.ModelEvaluator
import edu.twitter.model.impl.neuralnetwork.normal.NeuralNetworkBuilder
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark

/**
  * Application's entry point.
  *
  * This object will create stream of `ClassifiedTweets`
  * and persist them to elastic search, we can use
  * `kibana` to visualize the sentiment results.
  *
  */
object SentimentAnalyzer {
  def main(args: Array[String]): Unit = {
    implicit val appConfig: AppConfig = if (args.head == "dev") DevConfig else ProdConfig

    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    conf.set("es.index.auto.create", "true")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, appConfig.streamingInterval)

    new NeuralNetworkBuilder(sc).build()

    /*val indexHandler = new IndexHandler
    val indexCreationResult = indexHandler.create("twitter", "sentiment")

    indexCreationResult match {
      case Left(_) => System.exit(0)
      case Right(indexName) => runSentimentAnalyzer(indexName)
    }*/
  }

  private def runSentimentAnalyzer(indexName: String)(implicit appConfig: AppConfig): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    conf.set("es.index.auto.create", "true")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, appConfig.streamingInterval)
    val models = new Models(sc)

    val modelService = new ModelService(models)
    modelService.start()

    if (appConfig.evaluateModels) {
      new ModelEvaluator(sc).evaluateWithCorrection(models.allModelNames)
    }

    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream(models.allModelNames)
    classifiedStream.foreachRDD(EsSpark.saveToEs(_, indexName))

    ssc.start()
    ssc.awaitTermination()

    if (ssc != null) ssc.stop(true)
    if (sc != null) sc.stop()
    modelService.stop()
  }
}
