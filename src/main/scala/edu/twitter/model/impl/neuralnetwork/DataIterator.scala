package edu.twitter.model.impl.neuralnetwork

import java.io.IOException
import java.util
import java.util.NoSuchElementException

import com.typesafe.scalalogging.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.{INDArrayIndex, NDArrayIndex}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
  * An Iterator used to convert the training (or the testing) data to the format
  * that the neural network model understands.
  *
  * @param data           The labeled tweets
  * @param wordVectors    WordVectors object
  * @param batchSize      Size of each minibatch for training
  * @param truncateLength If reviews exceed
  */
@throws[IOException]
class DataIterator(val data: RDD[Row],
                   val wordVectors: WordVectors,
                   val batchSize: Int,
                   val truncateLength: Int) extends DataSetIterator {

  private final val vectorSize = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length

  private val logger = Logger(LoggerFactory.getLogger(classOf[DataIterator]))
  private val dataList = data.collect()
  final private val tokenizerFactory: TokenizerFactory = new DefaultTokenizerFactory
  tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor)

  private var dataCursor: Int = 0

  /**
    * Returns a DataSet contains specific number of data tuples
    *
    * @param num the specified number of the tuples
    * @return DataSet contains num tuples
    */
  def next(num: Int): DataSet = {
    if (dataCursor >= dataList.length)
      throw new NoSuchElementException
    try
      nextDataSet(num)
    catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  /**
    * Read the next num examples and transform them into a DataSet of features and label by
    * tokenizing the tweet's text and convert it to set of vectors using the word2vec model.
    *
    * @param num the number of examples
    * @return DataSet containing the features and labels
    */
  @throws[IOException]
  private def nextDataSet(num: Int): DataSet = {
    val tweets = new ArrayBuffer[String]()
    val positive = new ArrayBuffer[Boolean]()
    var i = 0

    while (i < num && dataCursor < totalExamples) {

      val msg = dataList(dataCursor).getAs[String]("msg")
      val label = dataList(dataCursor).getAs[Double]("label")

      //TODO: Find why some rows return null.
      if (msg != null) {
        tweets.append(msg)
        positive.append(if (label == 1.0) true else false)
        i += 1
      } else {
        logger.warn("Invalid tweet:" + dataList(dataCursor))
      }

      dataCursor += 1
    }

    val allTokens = removeUnknownWords(tweets)
    var maxLength = allTokens.maxBy(_.length).length

    // Workaround, just in case the word2vec doesn't recognise all the words in the batch which is unlikely to happen as we are using Google word2vec.
    if (maxLength == 0) {
      logger.warn(s"The model didn't recognise any word $tweets")
      allTokens(0).append("times")
      maxLength = 1
    }

    //If longest review exceeds 'truncateLength': only take the first 'truncateLength' words
    if (maxLength > truncateLength) maxLength = truncateLength

    transformToDataSet(allTokens, positive, maxLength)
  }

  /**
    * Tokenize the tweets and filter out the unknown words
    *
    * @param tweets the tweets to be filtered
    * @return the filtered tweets
    */
  private def removeUnknownWords(tweets: ArrayBuffer[String]): ArrayBuffer[mutable.Buffer[String]] = {
    val allTokens = new ArrayBuffer[mutable.Buffer[String]](tweets.size)
    import scala.collection.JavaConversions._
    for (s <- tweets) {
      val tokens = tokenizerFactory.create(s).getTokens
      val tokensFiltered = tokens.filter(wordVectors.hasWord)
      allTokens.append(tokensFiltered)
    }
    allTokens
  }

  /**
    * Transform the tweet to a DataSet the contains the extracted Feature of the tweet and its label
    *
    * @param allTokens the filtered tweets
    * @param positive  the label of a tweet
    * @param maxLength the maximum length of a tweet
    * @return DataSet contains both the features and the label of the tweets
    */
  private def transformToDataSet(allTokens: ArrayBuffer[mutable.Buffer[String]], positive: ArrayBuffer[Boolean], maxLength: Int): DataSet = {
    val features = Nd4j.create(allTokens.size, vectorSize, maxLength)
    val labels = Nd4j.create(allTokens.size, 2, maxLength)
    //Two labels: positive or negative because we are dealing with reviews of different lengths and only one output at the final time step: use padding arrays mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
    val featuresMask = Nd4j.zeros(allTokens.size, maxLength)
    val labelsMask = Nd4j.zeros(allTokens.size, maxLength)

    val temp = new Array[Int](2)
    for (i <- allTokens.indices) {
      val tokens = allTokens(i)
      temp(0) = i
      //Get word vectors for each word in review, and put them in the training data
      for (j <- 0 until math.min(tokens.size, maxLength)) {
        val token = tokens(j)
        val vector = wordVectors.getWordVectorMatrix(token)
        features.put(Array[INDArrayIndex](NDArrayIndex.point(i), NDArrayIndex.all, NDArrayIndex.point(j)), vector)

        temp(1) = j
        featuresMask.putScalar(temp, 1.0) //Word is present (not padding) for this example + time step -> 1.0 in features mask
      }

      val idx = if (positive(i)) 0 else 1
      val lastIdx: Int = Math.min(tokens.size, maxLength)
      labels.putScalar(Array[Int](i, idx, lastIdx - 1), 1.0) //Set label: (_, 1, _) to 1 for negative & (_, 0, _) to 1 for positive
      labelsMask.putScalar(Array[Int](i, lastIdx - 1), 1.0) //Specify that an output exists at the final time step for this example
    }

    new DataSet(features, labels, featuresMask, labelsMask)
  }

  def totalExamples: Int =
    dataList.length

  def inputColumns: Int =
    vectorSize

  def totalOutcomes: Int =
    2

  def reset(): Unit =
    dataCursor = 0

  def resetSupported: Boolean =
    true

  def asyncSupported: Boolean =
    true

  def batch: Int =
    batchSize

  def cursor: Int =
    dataCursor

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

}