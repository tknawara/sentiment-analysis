package edu.twitter.model.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.fasterxml.jackson.databind.ObjectMapper
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import edu.twitter.model.api.GenericModelBuilder
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContextExecutor, Future}

/** Representation of the post body */
case class ModelRequestBody(tweetMsg: String)

/** Helper object that contains the implicit json
  * converter. */
object ModelRequestBodySupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ModelRequestBodyFormat: RootJsonFormat[ModelRequestBody] = jsonFormat1(ModelRequestBody)
}

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
    import ModelRequestBodySupport._

    val models = builders.par.map(_.build())
    val routes = for (model <- models) yield {
      path(s"${model.name}" / "classify") {
        post {
          entity(as[ModelRequestBody]) { modelRequestBody =>
            val label = model.getLabel(modelRequestBody.tweetMsg)
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