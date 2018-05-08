package edu.twitter.model.impl.neuralnetwork.correct

import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.neuralnetwork.normal.NeuralNetworkModel
import edu.twitter.service.SpellingCorrectionService
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

class NeuralNetworkCorrectSpellingModel(model: MultiLayerNetwork, wordVectors: WordVectors)
  extends NeuralNetworkModel(model, wordVectors) {

  override val name: String = NeuralNetworkCorrectSpellingModel.name

  override def getLabel(tweetText: String): Label = {
    val correctTweet = SpellingCorrectionService.correctSpelling(tweetText)
    super.getLabel(correctTweet)
  }
}

/** Holder of the name of neural network
  * with spelling correction model. */
object NeuralNetworkCorrectSpellingModel {
  val name = "NeuralNetworkCorrectSpellingModel"
}
