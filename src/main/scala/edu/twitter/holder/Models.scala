package edu.twitter.holder

import edu.twitter.config.AppConfig
import edu.twitter.model.api.GenericModel
import edu.twitter.model.impl.gradientboosting.GradientBoostingBuilder
import edu.twitter.model.impl.neuralnetwork.NeuralNetworkBuilder
import edu.twitter.model.impl.textblob.TextBlobService
import org.apache.spark.SparkContext

import scala.collection.parallel.immutable.ParSeq

class Models(sc: SparkContext)(implicit appConfig: AppConfig) extends ModelsHolder {

  private val gradientBoosting = () => new GradientBoostingBuilder(sc)
    .build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedGradientBoostingModelPath,
      "GradientBoosting")

  private val gradientBoostingWithCorrectSpelling = () => new GradientBoostingBuilder(sc)
    .build(appConfig.paths.correctSpellingTrainingDataPath,
      appConfig.paths.savedGradientBoostingModelCorrectPath,
      "GradientBoostingWithCorrectSpelling")

  private val neuralNetwork = () => new NeuralNetworkBuilder(sc)
    .build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedNeuralNetworkModelPath,
      "NeuralNetwork")

  private val neuralNetworkWithCorrectSpelling = () => new NeuralNetworkBuilder(sc)
    .build(appConfig.paths.correctSpellingTrainingDataPath,
      appConfig.paths.savedNeuralNetworkModelCorrectPath,
      "NeuralNetworkWithCorrectSpelling")

  lazy val allModels: ParSeq[GenericModel] = List(gradientBoosting,
    gradientBoostingWithCorrectSpelling, neuralNetwork, neuralNetworkWithCorrectSpelling).par.map(_.apply())

  lazy val allModelNames: List[String] = List("GradientBoosting",
    "GradientBoostingWithCorrectSpelling", "NeuralNetwork", "NeuralNetworkWithCorrectSpelling", TextBlobService.name)
}
