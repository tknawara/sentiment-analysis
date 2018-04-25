package edu.twitter.model.client

import java.nio.charset.Charset

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.http.entity.StringEntity
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelClientSuite extends FunSuite {

  test("test jackson") {
    val text = "RT @ChildhoodShows: Before there was Troy Bolton there was Eddie Thomas https://t.co/QlxfSvtojc"
    val modelRequestBody = new ModelRequestBody(text)
    val objectMapper = new ObjectMapper()

    val res = objectMapper.writeValueAsString(modelRequestBody)
    val ss = new StringEntity(res)
    println(res)
    println(ss)
    println(IOUtils.toString(ss.getContent, Charset.defaultCharset()))
    assert(res != null)
  }
}
