package edu.twitter.classification

import edu.twitter.config.{AppConfig, DevConfig}
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.impl.gradientboosting.normal.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.service.ModelService
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.GenSeq

object ClassifierTest {
  def main(args: Array[String]): Unit = {
    implicit val appConfig: AppConfig = DevConfig

    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, appConfig.streamingInterval)

    class InnerModelsHolder extends ModelsHolder {
      lazy val allModels: GenSeq[GenericModel] = List(new GradientBoostingBuilder(sc)).par.map(_.build())
      lazy val allModelNames: List[String] = List(GradientBoostingModel.name)
    }

    val models = new InnerModelsHolder
    val modelService = new ModelService(models)
    modelService.start()

    val classifier = new Classifier(ssc)
    val classifiedStream = classifier.createClassifiedStream(models.allModelNames)
    classifiedStream.foreachRDD(rdd => rdd.take(10).foreach(println(_)))

    ssc.start()
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }
}
