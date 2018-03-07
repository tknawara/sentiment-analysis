package edu.twitter.classification

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.twitter.model.impl.GradientBoostingBuilder
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.ExecutionContextExecutor

object ClassifierTest {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("twitter-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    val modelService = new ModelService(new GradientBoostingBuilder(sc))
    modelService.start()

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
