package edu.twitter.neuralNetworkModel

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.{INDArrayIndex, NDArrayIndex}
import org.apache.spark.sql.{DataFrame, Row}


import java.io.{IOException}
import java.util
import java.util.NoSuchElementException

import scala.collection.JavaConverters._


/**
  * @param data           the tweets
  * @param wordVectors    WordVectors object
  * @param batchSize      Size of each minibatch for training
  * @param truncateLength If reviews exceed
  */
@throws[IOException]
class DataIterator(val data: DataFrame,
                   val wordVectors: WordVectors,
                   val batchSize: Int,
                   val truncateLength: Int) extends DataSetIterator {

  private final val vectorSize = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length

  private val dataList = data.collectAsList()
  final private val tokenizerFactory: TokenizerFactory = new DefaultTokenizerFactory
  tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor)

  private var _cursor: Int = 0

  def next(num: Int): DataSet = {
    if (_cursor >= dataList.size())
      throw new NoSuchElementException
    try {
      nextDataSet(num)
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  @throws[IOException]
  private def nextDataSet(num: Int): DataSet = {
    //First: load reviews to String. Alternate positive and negative reviews
    val reviews = new util.ArrayList[String](num)
    val positive = new Array[Boolean](num)
    var i = 0

    while (i < num && _cursor < totalExamples) {

      val msg = dataList.get(_cursor).getAs[String]("msg")
      val label = dataList.get(_cursor).getAs[Double]("label")

      reviews.add(msg)
      positive(i) = if (label == 1.0) true else false

      _cursor += 1
      i += 1
    }

    //Second: tokenize reviews and filter out unknown words
    val allTokens: util.List[util.List[String]] = new util.ArrayList[util.List[String]](reviews.size)
    var maxLength: Int = 0
    import scala.collection.JavaConversions._
    for (s <- reviews; if s != null) {
      //println(s)
      val tokens = tokenizerFactory.create(s).getTokens
      val tokensFiltered = new util.ArrayList[String]
      for (t <- tokens.asScala) {
        if (wordVectors.hasWord(t)) tokensFiltered.add(t)
      }
      allTokens.add(tokensFiltered)
      maxLength = Math.max(maxLength, tokensFiltered.size)
    }

    //If longest review exceeds 'truncateLength': only take the first 'truncateLength' words
    if (maxLength > truncateLength) maxLength = truncateLength

    //Create data for training
    //Here: we have reviews.size() examples of varying lengths
    val features = Nd4j.create(allTokens.size, vectorSize, maxLength)
    val labels = Nd4j.create(allTokens.size, 2, maxLength)
    //Two labels: positive or negative
    //Because we are dealing with reviews of different lengths and only one output at the final time step: use padding arrays
    //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
    val featuresMask = Nd4j.zeros(allTokens.size, maxLength)
    val labelsMask = Nd4j.zeros(allTokens.size, maxLength)

    val temp = new Array[Int](2)
    for (i <- allTokens.indices) {
      val tokens: util.List[String] = allTokens.get(i)
      temp(0) = i
      //Get word vectors for each word in review, and put them in the training data
      var j: Int = 0
      for (j <- 0 until math.min(tokens.size, maxLength)) {
        val token = tokens.get(j)
        val vector = wordVectors.getWordVectorMatrix(token)
        features.put(Array[INDArrayIndex](NDArrayIndex.point(i), NDArrayIndex.all, NDArrayIndex.point(j)), vector)

        temp(1) = j
        featuresMask.putScalar(temp, 1.0) //Word is present (not padding) for this example + time step -> 1.0 in features mask
      }

      val idx = if (positive(i)) 0 else 1
      val lastIdx: Int = Math.min(tokens.size, maxLength)
      labels.putScalar(Array[Int](i, idx, lastIdx - 1), 1.0) //Set label: [0,1] for negative, [1,0] for positive
      labelsMask.putScalar(Array[Int](i, lastIdx - 1), 1.0) //Specify that an output exists at the final time step for this example
    }
    new DataSet(features, labels, featuresMask, labelsMask)
  }

  def totalExamples: Int =
    dataList.size()

  def inputColumns: Int =
    vectorSize

  def totalOutcomes: Int =
    2

  def reset(): Unit =
    _cursor = 0

  def resetSupported: Boolean =
    true

  def asyncSupported: Boolean =
    true

  def batch: Int =
    batchSize

  def cursor: Int =
    _cursor

  def numExamples: Int =
    totalExamples

  def setPreProcessor(preProcessor: DataSetPreProcessor): Unit =
    throw new UnsupportedOperationException

  def getLabels: util.List[String] =
    util.Arrays.asList("positive", "negative")

  def hasNext: Boolean =
    cursor < numExamples

  def next: DataSet =
    next(batchSize)

  override def remove(): Unit =
    ()

  def getPreProcessor: DataSetPreProcessor =
    throw new UnsupportedOperationException("Not implemented")

  /**
    * Used post training to convert a String to a features INDArray that can be passed to the network output method
    *
    * @param reviewContents Contents of the review to vectorize
    * @param maxLength      Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
    * @return Features array for the given input String
    */
  def loadFeaturesFromString(reviewContents: String, maxLength: Int): INDArray = {
    val tokens = tokenizerFactory.create(reviewContents).getTokens
    val tokensFiltered = new util.ArrayList[String]
    for (t <- tokens.asScala) {
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