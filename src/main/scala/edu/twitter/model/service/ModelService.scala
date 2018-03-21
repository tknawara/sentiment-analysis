package edu.twitter.model.service

import java.nio.charset.Charset
import java.util.Base64
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.fasterxml.jackson.databind.ObjectMapper
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import edu.twitter.model.api.GenericModelBuilder
import scala.concurrent.{ExecutionContextExecutor, Future}


/**
  * Exposes a Rest API for accessing the model.
  *
  * @param builders Seq holding the recipe for building
  *                 the models.
  */
class ModelService(builders: Seq[GenericModelBuilder]) {
  require(builders.nonEmpty)

  private var bindingFuture: Future[ServerBinding] = _

  implicit val system: ActorSystem = ActorSystem("twitter-actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  def start(): Unit = {
    val models = builders.map(_.build())
    val routes = for (model <- models) yield {
      path(s"${model.name}" / "classify") {
        get {
          parameters('tweet.as[String]) { tweet =>
            val decodedTweet = new String(Base64.getUrlDecoder.decode(tweet), Charset.forName("UTF-16"))
            val label = model.getLabel(decodedTweet)
            complete(ModelService.objectMapper.writeValueAsString(label))
          }
        }
      }
    }

    val route = routes.reduce(_ ~ _)
    bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
  }

  def stop(): Unit = {
    if (bindingFuture != null) {
      bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }
  }
}

object ModelService {
  val objectMapper = new ObjectMapper()

}