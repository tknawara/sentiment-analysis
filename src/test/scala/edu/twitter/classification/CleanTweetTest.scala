package edu.twitter.classification

object CleanTweetTest {
  def main(args: Array[String]): Unit = {
    val tweet = "FlemingYoung happy bday to #my boy C https//tco/LmjhzggruZ"
    val res = tweet.split(" ").filter(isValid).mkString(" ")
    println(res)
  }

  /** Validate the token from the tweet message */
  private def isValid(s: String): Boolean = {
    // @tarek-nawara Only checking for url patterns, mentions, hashtags and retweets
    // should add more validation criteria in the future see issue #13
    !(s.contains("http") || s.contains("@") || s.contains("RT") || s.contains("#"))
  }

}
