package edu.twitter.model

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.twitter.model.service.ModelService
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.ExecutionContextExecutor

object ModelClientServiceTest {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val conf = new SparkConf().setMaster("local[*]").setAppName("Twitter")
    val sc = new SparkContext(conf)

    val modelService = new ModelService()
    modelService.exposeModel()
    val modelClient = new ModelClient()
  }
}
