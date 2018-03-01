package edu.twitter.neuralNetworkModel

import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.nn.conf.{GradientNormalization, MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.conf.layers.{GravesLSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.lossfunctions.LossFunctions
import java.io._
import org.apache.spark.sql.functions.rand

import org.apache.spark.SparkContext

@SerialVersionUID(100L)
class Word2VecSentimentNN(sc: SparkContext) extends Serializable {

  /** Location (local file system) for the Google News vectors. */
  //val WORD_VECTORS_PATH: String = this.getClass.getClassLoader.getResource("NewsModel.txt").getPath
  val WORD_VECTORS_PATH: String = this.getClass.getClassLoader.getResource("GoogleNews-vectors-negative300.bin.gz").getPath

  val batchSize = 256 //Number of examples in each minibatch
  val nEpochs = 100 //Number of epochs (full passes of training data) to train on
  val truncateReviewsToLength = 280 //Truncate reviews with length (# words) greater than this

  //DataSetIterators for training and testing respectively
  //val wordVectors = WordVectorSerializer.loadTxtVectors(new File(WORD_VECTORS_PATH))
  val wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH))
  val vectorSize: Int = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length // 100 in our case

  val data = (new TweetsLoader(sc).getTweetsDataSet()).orderBy(rand())
  val Array(trainData, testData) = data.randomSplit(Array(0.7, 0.3))
  val train = new DataIterator(trainData, wordVectors, batchSize, truncateReviewsToLength)
  val test = new DataIterator(testData, wordVectors, batchSize, truncateReviewsToLength)

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

  def getLabel(tweet: String): Double = {
    val features = test.loadFeaturesFromString(tweet, truncateReviewsToLength)
    val networkOutput = net.output(features)
    val timeSeriesLength = networkOutput.size(2)
    val probabilities = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(timeSeriesLength - 1))
    val happy = probabilities.getDouble(0)
    val sad = probabilities.getDouble(1)
    if (happy > sad) 1.0 else 0.0
  }

}