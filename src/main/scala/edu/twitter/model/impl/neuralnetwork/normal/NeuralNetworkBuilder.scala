package edu.twitter.model.impl.neuralnetwork.normal

import edu.twitter.config.AppConfig
import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.impl.neuralnetwork.NeuralNetworkBaseBuilder
import org.apache.spark.SparkContext

class NeuralNetworkBuilder(sc: SparkContext)(implicit appConfig: AppConfig)
  extends NeuralNetworkBaseBuilder(sc)
    with GenericModelBuilder {

  /**
    * Run the recipe responsible for constructing the model.
    *
    * @return an instance of generic model.
    */
  override def build(): GenericModel = {
    val baseModel = super.build(appConfig.paths.trainingDataPath,
      appConfig.paths.savedNeuralNetworkModelPath,
      NeuralNetworkModel.name)
    new NeuralNetworkModel(baseModel, appConfig.wordVectors)
  }
}
