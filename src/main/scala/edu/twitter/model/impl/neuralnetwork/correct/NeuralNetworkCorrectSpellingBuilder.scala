package edu.twitter.model.impl.neuralnetwork.correct

import edu.twitter.config.AppConfig
import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.impl.neuralnetwork.NeuralNetworkBaseBuilder
import org.apache.spark.SparkContext

/** Builder for Neural network correct spelling model.
  *
  * @param sc        Spark Context.
  * @param appConfig application configuration
  */
class NeuralNetworkCorrectSpellingBuilder(sc: SparkContext)(implicit appConfig: AppConfig)
  extends NeuralNetworkBaseBuilder(sc)
    with GenericModelBuilder {

  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  override def build(): GenericModel = {
    val baseModel = super.build(appConfig.paths.correctSpellingTrainingDataPath,
      appConfig.paths.savedNeuralNetworkModelCorrectPath,
      NeuralNetworkCorrectSpellingModel.name)
    new NeuralNetworkCorrectSpellingModel(baseModel, appConfig.wordVectors)
  }
}
