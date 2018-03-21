package edu.twitter.model.service

import java.nio.charset.Charset
import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.fasterxml.jackson.databind.ObjectMapper
import edu.twitter.model.api.GenericModelBuilder

import scala.concurrent.ExecutionContext



/**
  * Exposes a Rest API for accessing the model.
  *
  * @param genericModelBuilder instance holding the recipe for building
  *                            the model.
  */
class ModelService(genericModelBuilder: GenericModelBuilder)
                  (implicit val executionContext: ExecutionContext,
                   implicit val system: ActorSystem,
                   implicit val materializer: Materializer) {


  def start(): Unit = {
    val model = genericModelBuilder.build()
    val route: Route =
      path(s"${model.name}" / "classify") {
        get {
          parameters('tweet.as[String]) { tweet =>
            val decodedTweet = new String(Base64.getUrlDecoder.decode(tweet), Charset.forName("UTF-16"))
            val label = model.getLabel(decodedTweet)
            complete(ModelService.objectMapper.writeValueAsString(label))
          }
        }
      }

    Http().bindAndHandle(route, "localhost", 8080)
  }

}

object ModelService {
  val objectMapper = new ObjectMapper()
}