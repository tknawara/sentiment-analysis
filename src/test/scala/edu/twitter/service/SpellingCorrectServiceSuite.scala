package edu.twitter.service

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SpellingCorrectServiceSuite extends FunSuite {

  test("should correct spelling mistakes in tweet") {
    val tweetMessage = "I hav a goood spelling"
    val correctSpelling = SpellingCorrectionService.correctSpelling(tweetMessage)
    val correct = "I had a good spelling"
    assert(correctSpelling == correct)
  }

  test("should handle replacing a shorter word with a longer one") {
    val tweetMessage = "I hd a good speling"
    val correctSpelling = SpellingCorrectionService.correctSpelling(tweetMessage)
    val correct = "I had a good spelling"
    assert(correctSpelling == correct)
  }
}
