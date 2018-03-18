package edu.twitter.classification

import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object ClassifierTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    val modelService = new ModelService(List(new GradientBoostingBuilder(sc)))
    modelService.start()

    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream(GradientBoostingModel.name)
    classifiedStream.foreachRDD(rdd => rdd.take(10).foreach(println(_)))

    ssc.start()
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }
}
