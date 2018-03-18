package edu.twitter.model.service

import java.nio.charset.Charset
import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import edu.twitter.model.api.GenericModelBuilder
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContextExecutor

/** Representation of the model's response.
  * This case class will be converted to Json and
  * returned as a response. */
case class TweetLabel(label: Double)

/**
  * Exposes a Rest API for accessing the model.
  *
  * @param genericModelBuilder instance holding the recipe for building
  *                            the model.
  */
class ModelService(genericModelBuilder: GenericModelBuilder) {
  implicit val system: ActorSystem = ActorSystem("twitter-actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val tweetLabelFormat: RootJsonFormat[TweetLabel] = jsonFormat1(TweetLabel)

  def start(): Unit = {
    val model = genericModelBuilder.build()
    val route: Route =
      path(s"${model.name}" / "classify") {
        get {
          parameters('tweet.as[String]) { tweet =>
            val decodedTweet = new String(Base64.getUrlDecoder.decode(tweet), Charset.forName("UTF-16"))
            complete(TweetLabel(model.getLabel(decodedTweet)))
          }
        }
      }

    Http().bindAndHandle(route, "localhost", 8080)
  }

}