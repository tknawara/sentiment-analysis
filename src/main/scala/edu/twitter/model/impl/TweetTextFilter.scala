package edu.twitter.model.impl

/** Responsible for filtering the tweet
  * text and removing tokens that could affect
  * the classification accuracy.
  */
object TweetTextFilter {

  /**
    * Remove all invalid tokens from tweet.
    *
    * @param tweet tweet text
    * @return filtered tweet
    */
  def filterTweet(tweet: String): String = {
    "\\b(\\S*?)(.)\\2{2,}\\b".r.replaceAllIn(tweet.replaceAll("https?:\\/\\/\\S+\\b|www\\.(\\w+\\.)+\\S*", " <URL> ")
      .replaceAll("/", " / ")
      .replaceAll("@\\w+", " <USER> ")
      .replaceAll("[8:=;]['`\\-]?[)d]+|[)d]+['`\\-]?[8:=;]", " <SMILE> ")
      .replaceAll("[8:=;]['`\\-]?p+", " <LOLFACE> ")
      .replaceAll("[8:=;]['`\\-]?\\(+|\\)+['`\\-]?[8:=;]", " <SADFACE> ")
      .replaceAll("[8:=;]['`\\-]?[\\/|l*]", " <NEUTRALFACE> ")
      .replaceAll("<3", " <HEART> ")
      .replaceAll("[-+]?[.\\d]*[\\d]+[:,.\\d]*", " <NUMBER> ")
      .replaceAll("!!+", " ! <REPEAT> ")
      .replaceAll("\\?\\?+", " ? <REPEAT> ")
      .replaceAll("\\.\\.+", " . <REPEAT> "), m => " " + m.group(1) + m.group(2) + " <ELONG> ")
  }
}
