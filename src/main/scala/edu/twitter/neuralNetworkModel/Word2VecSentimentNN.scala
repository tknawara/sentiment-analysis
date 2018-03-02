package edu.twitter.neuralNetworkModel

import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.nn.conf.{GradientNormalization, MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.conf.layers.{GravesLSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.indexing.{INDArrayIndex, NDArrayIndex}
import org.nd4j.linalg.lossfunctions.LossFunctions

import java.io._
import java.util

import org.apache.spark.sql.functions.rand
import org.apache.spark.SparkContext
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

@SerialVersionUID(100L)
class Word2VecSentimentNN(sc: SparkContext) extends Serializable {

  /** Location (local file system) for the Google News vectors. */
  val WORD_VECTORS_PATH: String = this.getClass.getClassLoader.getResource("NewsModel.txt").getPath
  //val WORD_VECTORS_PATH: String = this.getClass.getClassLoader.getResource("GoogleNews-vectors-negative300.bin.gz").getPath

  val batchSize = 256 //Number of examples in each minibatch
  val nEpochs = 1 //Number of epochs (full passes of training data) to train on
  val truncateReviewsToLength = 280 //Truncate reviews with length (# words) greater than this

  //DataSetIterators for training and testing respectively
  val wordVectors = WordVectorSerializer.loadTxtVectors(new File(WORD_VECTORS_PATH))
  //val wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH))
  val vectorSize: Int = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length // 100 in our case

  val data = (new TweetsLoader(sc).getTweetsDataSet()).orderBy(rand())
  val Array(trainData, testData) = data.randomSplit(Array(0.01, 0.99))
  @transient val train = new DataIterator(trainData, wordVectors, batchSize, truncateReviewsToLength)
  @transient val test = new DataIterator(testData, wordVectors, batchSize, truncateReviewsToLength)

  //Set up network configuration
  val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
    .updater(Updater.ADAM)
    .regularization(true)
    .l2(1e-5)
    .weightInit(WeightInit.XAVIER)
    .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
    .gradientNormalizationThreshold(1.0)
    .learningRate(0.5)
    .list
    .layer(0, new GravesLSTM.Builder().nIn(vectorSize).nOut(256)
      .activation(Activation.TANH)
      .build())
    .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
      .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build)
    .pretrain(false).backprop(true).build()

  val net = new MultiLayerNetwork(conf)
  net.init()
  net.setListeners(new ScoreIterationListener(100))

  println("Starting training")
  for (i <- 0 until nEpochs) {
    net.fit(train)
    train.reset()

    val evaluation = new Evaluation(2)
    while (test.hasNext) {
      val t = test.next
      val features = t.getFeatureMatrix
      val labels = t.getLabels
      val inMask = t.getFeaturesMaskArray
      val outMask = t.getLabelsMaskArray
      val predicted = net.output(features, false, inMask, outMask)
      evaluation.evalTimeSeries(labels, predicted, outMask)
    }
    test.reset()

    println(evaluation.stats)
  }

  //ModelSerializer.writeModel(net, this.getClass().getClassLoader().getResource("savedModels").getPath() + File.separator + "NeuralNetworkModel.net", true)

  def getLabel(tweet: String): Double = {
    val features = loadFeaturesFromString(tweet, truncateReviewsToLength)
    val networkOutput = net.output(features)
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
    * @param maxLength      Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
    * @return Features array for the given input String
    */
  def loadFeaturesFromString(reviewContents: String, maxLength: Int): INDArray = {

    val tokenizerFactory: TokenizerFactory = new DefaultTokenizerFactory
    tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor)

    import scala.collection.JavaConversions._
    val tokens = tokenizerFactory.create(reviewContents).getTokens
    val tokensFiltered = new util.ArrayList[String]
    for (t <- tokens) {
      if (wordVectors.hasWord(t)) tokensFiltered.add(t)
    }
    val outputLength = Math.max(maxLength, tokensFiltered.size)

    val features = Nd4j.create(1, vectorSize, outputLength)
    for (j <- 0 until math.min(tokens.size, maxLength)) {
      val token = tokens.get(j)
      val vector = wordVectors.getWordVectorMatrix(token)
      features.put(Array[INDArrayIndex](NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(j)), vector)
    }
    features
  }

}