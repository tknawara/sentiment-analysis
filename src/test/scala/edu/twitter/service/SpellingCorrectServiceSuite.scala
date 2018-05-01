package edu.twitter.service

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SpellingCorrectServiceSuite extends FunSuite {

  test("should correct spelling mistakes in tweet") {
    val tweetMessage = "I havv a goood spelling"
    val correctSpelling = SpellingCorrectionService.correctSpelling(tweetMessage)
    assert(correctSpelling.nonEmpty)
    val correct = "I have a good spelling"
    assert(correctSpelling.get == correct)
  }
}
