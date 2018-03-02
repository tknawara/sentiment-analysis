package edu.twitter.model

import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import scala.util.Try

/**
  * Get training and testing data model from tweetsRDD
  *
  * @param tweetsRDD RDD contain stream of tweets
  */
class SentimentModelDataCreator(tweetsRDD: RDD[Row]) {

  /**
    * Load the labeled tweets then transform each tweet's text to
    * a feature vector using a suitable transformation function
    *
    * @return training data set, testing data set
    */
  def getTrainingAndTestingData(): (RDD[LabeledPoint], RDD[LabeledPoint]) = {
    //We use scala's Try to filter out tweets that couldn't be parsed
    val labeledTweets = getLabeledRecords()
    val transformedTweets = transformData(labeledTweets)

    // Split the data into training and validation sets (30% held out for validation testing)
    val splits = transformedTweets.randomSplit(Array(0.7, 0.3))
    (splits(0), splits(1))
  }

  /**
    * Gradient Boosting expects as input a vector (feature array) of fixed length,
    * so we need a way to convert our tweets into some numeric vector that represents that tweet.
    * A standard way to do this is to use the hashing trick, in which we hash each word and index
    * it into a fixed-length array. What we get back is an array that represents the count of each
    * word in the tweet. This approach is called the bag of words model, which means we are representing
    * each sentence or document as a collection of discrete words and ignore grammar or the order in
    * which words appear in a sentence. An alternative approach to bag of words would be to use an algorithm
    * like Doc2Vec or Latent Semantic Indexing, which would use machine learning to build a vector representations
    * of tweets.In Spark using HashingTF for feature hashing. Note that we’re using an array of size 2000.
    * Since this is smaller than the size of the vocabulary we’ll encounter on Twitter, it means two words with
    * different meaning can be hashed to the same location in the array. Although it would seem this would be an issue,
    * in practice this preserves enough information that the model still works. This is actually one of the strengths
    * of feature hashing, that it allows you to represent a large or growing vocabulary in a fixed amount of space.
    *
    * @param labeledTweets
    * @return RDD of label (0 , 1) and sparse vector (ex: (1.0,(2000,[105,1139,1707,1872,1964],[1.0,1.0,1.0,1.0,1.0])))
    */
  def transformData(labeledTweets: RDD[(Double, Seq[String])]): RDD[LabeledPoint] = {
    //Transform data
    val hashingTF = new HashingTF(2000)

    //Map the input strings to a tuple of labeled point + input text
    val inputLabeled = labeledTweets.map(
      t => (t._1, hashingTF.transform(t._2)))
      .map(x => new LabeledPoint(x._1, x._2))
    inputLabeled
  }

  /**
    * Load the labeled tweets.
    *
    * @return labeled data
    */
  private def getLabeledRecords(): RDD[(Double, Seq[String])] = {
    val labeledTweets = tweetsRDD.map {
      record => (record.getAs[Double]("label"), record.getAs[String]("msg").split(" ").toSeq)
    }

    labeledTweets
  }
}
