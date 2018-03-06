package edu.twitter.model

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.client.ModelClient
import edu.twitter.model.service.ModelService

import scala.concurrent.ExecutionContextExecutor

object ModelClientServiceTest {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("twitter-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val modelService = new ModelService(new TestGenericModelBuilder())
    modelService.start()

    val resp = ModelClient.callModelService("hello")
    println(resp.get().getLabel)
  }

  class TestGenericModel extends GenericModel {
    override def name: String = "TestGenericModel"

    /**
      * Classify the given tweet.
      *
      * @param tweetText target tweet message for classification.
      * @return 0 for sad & 1 for happy
      */
    override def getLabel(tweetText: String): Double = 0.0
  }

  class TestGenericModelBuilder extends GenericModelBuilder {
    /**
      * Run the recipe responsible for constructing the model.
      *
      * @return an instance of generic model.
      */
    override def build(): GenericModel = new TestGenericModel()
  }
}
