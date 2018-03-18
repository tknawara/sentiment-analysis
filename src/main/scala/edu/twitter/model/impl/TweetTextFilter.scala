package edu.twitter.model.impl

object TweetTextFilter {
  private val invalidTokens = Set("http", "@", "#")

  def filterTweet(tweet: String): String = {
    tweet.split(" ").filter(s => invalidTokens.forall(!s.contains(_))).mkString(" ")
  }
}
