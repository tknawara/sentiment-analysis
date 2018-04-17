package edu.twitter.config

import java.io.File

/** Application Configuration
  * we can add any configurations here to
  * be used by all parts of the application. */
sealed trait AppConfig {

  /** defines wither we are running
    * in production or not. */
  def isProd: Boolean

  /** Number of epochs will be used
    * in training neural network model. */
  def neuralNetworkEpochs: Int

  /** If true we will use the model evaluator
    * to evaluate all models. */
  def evaluateModels: Boolean

  /** If true model evaluator will persist
    * the evaluation. */
  def persistEvaluation: Boolean

  /** Number of iterations used for
    * training gradient boosting model. */
  def gradientIterations: Int

  /** Depth of the trees of the gradient
    * boosting model. */
  def gradientDepth: Int

  /** Get the path of word vector for
    * neural model. */
  def wordVectorPath: String

  val paths: DataPaths.type = DataPaths
}

/** Holder for all paths used in the
  * Application. */
object DataPaths {
  lazy val savedNeuralNetworkModelPath: String = getAbsolutePath("saved-models") + File.separator + "NeuralNetworkModel.net"
  lazy val savedGradientBoostingModelPath: String = getAbsolutePath("saved-models") + File.separator + "GradientBoosting"
  lazy val newsModelPath: String = getAbsolutePath("NewsModel.txt")
  lazy val googleNewsPath: String = getAbsolutePath("GoogleNews-vectors-negative300.bin.gz")

  val trainingDataPath: String = getAbsolutePath("labeled-tweets")
  val validationDataPath: String = getAbsolutePath("labeled-tweets")

  /**
    *
    * @param source target source
    * @return absolute path of the target source
    */
  def getAbsolutePath(source: String): String = {
    this.getClass.getClassLoader.getResource(source).getPath
  }
}

/** Representation of the development
  * Configurations. */
object DevConfig extends AppConfig {
  val isProd = false
  val neuralNetworkEpochs = 1
  val evaluateModels = false
  val persistEvaluation = false
  val gradientIterations = 20
  val gradientDepth = 5
  val wordVectorPath: String = paths.newsModelPath
}

/** Representation of the production
  * Configurations. */
object ProdConfig extends AppConfig {
  val isProd = true
  val neuralNetworkEpochs = 10
  val evaluateModels = true
  val persistEvaluation = true
  val gradientIterations = 26
  val gradientDepth = 6
  val wordVectorPath: String = paths.googleNewsPath
}
