package edu.twitter.classification

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.impl.TestModelsHolder
import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object ClassifierTest {
  def main(args: Array[String]): Unit = {
    implicit val appConfig: AppConfig = DevConfig

    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, appConfig.streamingInterval)

    val models = new TestModelsHolder
    val modelService = new ModelService(models)
    modelService.start()

    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream(List("GradientBoosting"))
    classifiedStream.foreachRDD(rdd => rdd.take(10).foreach(println(_)))

    ssc.start()
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }
}
