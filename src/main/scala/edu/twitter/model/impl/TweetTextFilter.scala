package edu.twitter.model.impl

/** Responsible for filtering the tweet
  * text and removing tokens that could affect
  * the classification accuracy.
  */
object TweetTextFilter {
  private val invalidTokens = Set("http", "@", "#")

  /**
    * Remove all invalid tokens from tweet.
    *
    * @param tweet tweet text
    * @return filtered tweet
    */
  def filterTweet(tweet: String): String = {
    tweet.split(" ").filter(s => invalidTokens.forall(!s.contains(_))).mkString(" ")
  }
}
