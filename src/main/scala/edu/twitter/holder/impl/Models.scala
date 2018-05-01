package edu.twitter.holder.impl

import edu.twitter.config.AppConfig
import edu.twitter.holder.api.ModelsHolder
import edu.twitter.model.api.GenericModel
import edu.twitter.model.impl.gradientboosting.{GradientBoostingBuilder, GradientBoostingCorrectSpellingModel, GradientBoostingModel}
import edu.twitter.model.impl.neuralnetwork.{NeuralNetworkBuilder, NeuralNetworkCorrectSpellingModel, NeuralNetworkModel}
import edu.twitter.model.impl.textblob.TextBlobService
import org.apache.spark.SparkContext

import scala.collection.parallel.immutable.ParSeq

class Models(sc: SparkContext)(implicit appConfig: AppConfig) extends ModelsHolder {

  private val gradientBoosting = () => new GradientBoostingBuilder(sc)
    .build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedGradientBoostingModelPath,
      GradientBoostingModel.name)

  private val gradientBoostingWithCorrectSpelling = () => new GradientBoostingBuilder(sc)
    .build(appConfig.paths.correctSpellingTrainingDataPath,
      appConfig.paths.savedGradientBoostingModelCorrectPath,
      GradientBoostingCorrectSpellingModel.name)

  private val neuralNetwork = () => new NeuralNetworkBuilder(sc)
    .build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedNeuralNetworkModelPath,
      NeuralNetworkModel.name)

  private val neuralNetworkWithCorrectSpelling = () => new NeuralNetworkBuilder(sc)
    .build(appConfig.paths.correctSpellingTrainingDataPath,
      appConfig.paths.savedNeuralNetworkModelCorrectPath,
      NeuralNetworkCorrectSpellingModel.name)

  lazy val allModels: ParSeq[GenericModel] = List(gradientBoosting,
    gradientBoostingWithCorrectSpelling, neuralNetwork, neuralNetworkWithCorrectSpelling).par.map(_.apply())

  lazy val allModelNames: List[String] = List(GradientBoostingModel.name,
    GradientBoostingCorrectSpellingModel.name, NeuralNetworkModel.name,
    NeuralNetworkCorrectSpellingModel.name, TextBlobService.name)
}
