package edu.twitter.classification

import edu.twitter.model.impl.TweetTextFilter
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CleanTweetSuite extends FunSuite {
  test("applying filtering over the tweet text should remove references and mentions") {

    val tweet = "FlemingYoung happy bday to #my boy C https//tco/LmjhzggruZ"
    val res = TweetTextFilter.filterTweet(tweet)
    println(res)
    val filteredTweet = "FlemingYoung happy bday to boy C"
    assert(res == filteredTweet)
  }

}
