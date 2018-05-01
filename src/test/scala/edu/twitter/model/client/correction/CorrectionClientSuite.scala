package edu.twitter.model.client.correction

import edu.twitter.config.DevConfig
import edu.twitter.model.impl.textblob.TextBlobService
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Ignore}
import org.scalatest.junit.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class CorrectionClientSuite extends FunSuite {
  test("Correction client sanity test") {
    val tweet = "I'm hapy"
    val correct = "I'm happy"
    val res = CorrectionClient.callCorrectionService(DevConfig.modelServicePorts(TextBlobService.name),
      TextBlobService.name, tweet)
    assert(res.nonEmpty)
    assert(res.get == correct)
  }
}
