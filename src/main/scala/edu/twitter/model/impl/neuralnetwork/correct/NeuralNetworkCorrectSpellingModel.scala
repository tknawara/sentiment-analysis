package edu.twitter.model.impl.neuralnetwork.correct

import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.neuralnetwork.normal.NeuralNetworkModel
import edu.twitter.service.SpellingCorrectionService
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

/** Same as Neural network model except of the difference
  * of correcting the tweets before classification.
  *
  * @param model       actual model
  * @param wordVectors word2vec model used to extract the features
  */
class NeuralNetworkCorrectSpellingModel(model: MultiLayerNetwork, wordVectors: WordVectors)
  extends NeuralNetworkModel(model, wordVectors) {

  override val name: String = NeuralNetworkCorrectSpellingModel.name

  /** @inheritdoc*/
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
