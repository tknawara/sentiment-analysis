package edu.twitter.model.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

class ModelService(implicit val executionContext: ExecutionContext,
                   implicit val system: ActorSystem,
                   implicit val materializer: Materializer) {

  final case class TweetLabel(label: Double)
  implicit val tweetLabelFormat: RootJsonFormat[TweetLabel] = jsonFormat1(TweetLabel)

  def exposeModel(): Unit = {

    val route: Route =
      path("classify") {
        get {
          parameters('tweet.as[String]) { tweet =>
            complete(TweetLabel(1.0))
          }
        }
      } ~ path("test") {
        get {
          complete("Recieved Test")
        }
      }

    Http().bindAndHandle(route, "localhost", 8080)
  }
}