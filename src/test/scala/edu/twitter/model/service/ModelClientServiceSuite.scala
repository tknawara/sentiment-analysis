package edu.twitter.model.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.twitter.model.client.ModelClient
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContextExecutor

class ModelClientServiceSuite extends FunSuite {
  test("the model client's result should match the model's response") {
    implicit val system: ActorSystem = ActorSystem("twitter-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val modelService = new ModelService(new TestGenericModelBuilder())
    modelService.start()

    val modeName = "TestGenericModel"
    val tweet = "hello"
    val resp = ModelClient.callModelService(modeName, tweet)

    assert(resp.get().getLabel == 0)
  }
}
