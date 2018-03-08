package edu.twitter.model.impl.neuralnetwork

import edu.twitter.model.api.GenericModel
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.{INDArrayIndex, NDArrayIndex}

/**
  * Wrapper for Neural Network Model
  *
  * @param model       actual model
  * @param wordVectors word2vec model used to extract the features
  */
class NeuralNetworkModel(model: MultiLayerNetwork,
                         wordVectors: WordVectors) extends GenericModel {

  val name = "NeuralNetwork"
  val vectorSize = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Double = {
    val features = loadFeaturesFromString(tweetText)
    val networkOutput = model.output(features)
    val timeSeriesLength = networkOutput.size(2)
    val probabilities = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(timeSeriesLength - 1))
    val happy = probabilities.getDouble(0)
    val sad = probabilities.getDouble(1)
    if (happy > sad) 1.0 else 0.0
  }

  /**
    * Used post training to convert a String to a features INDArray that can be passed to the network output method
    *
    * @param reviewContents Contents of the review to vectorize
    * @return Features array for the given input String
    */
  def loadFeaturesFromString(reviewContents: String): INDArray = {

    val tokenizerFactory: TokenizerFactory = new DefaultTokenizerFactory
    tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor)

    import scala.collection.JavaConversions._
    val tokens = tokenizerFactory.create(reviewContents).getTokens
    val tokensFiltered = tokens.filter(wordVectors.hasWord)

    val features = Nd4j.create(1, vectorSize, math.max(tokensFiltered.size, 1))
    for (j <- 0 until tokensFiltered.size) {
      val token = tokensFiltered.get(j)
      val vector = wordVectors.getWordVectorMatrix(token)
      features.put(Array[INDArrayIndex](NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(j)), vector)
    }
    features
  }


}
