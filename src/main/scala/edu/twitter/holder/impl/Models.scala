package edu.twitter.holder.impl

import edu.twitter.config.AppConfig
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.impl.gradientboosting.correct.{GradientBoostingCorrectSpellingBuilder, GradientBoostingCorrectSpellingModel}
import edu.twitter.model.impl.gradientboosting.normal.{GradientBoostingBuilder, GradientBoostingModel}
import edu.twitter.model.impl.neuralnetwork.correct.{NeuralNetworkCorrectSpellingBuilder, NeuralNetworkCorrectSpellingModel}
import edu.twitter.model.impl.neuralnetwork.normal.{NeuralNetworkBuilder, NeuralNetworkModel}
import edu.twitter.model.impl.textblob.TextBlobService
import org.apache.spark.SparkContext

import scala.collection.parallel.immutable.ParSeq

/** Holder for all the application's models.
  *
  * @param sc        spark context
  * @param appConfig holder for application configurations
  */
class Models(sc: SparkContext)(implicit appConfig: AppConfig) extends ModelsHolder {

  lazy val allModels: ParSeq[GenericModel] =
    List(new GradientBoostingBuilder(sc),
      new NeuralNetworkBuilder(sc))

  lazy val allModelNames: List[String] = List(GradientBoostingModel.name,
    NeuralNetworkModel.name, TextBlobService.name)
}
