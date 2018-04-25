package edu.twitter.index

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure}
import com.typesafe.scalalogging.Logger
import edu.twitter.config.AppConfig

/**
  * Class responsible for creating ElasticSearch index with specified mappings.
  */
class IndexHandler(implicit appConfig: AppConfig) {

  private val logger = Logger(classOf[IndexHandler])
  private val IndexAlreadyExistsErrorMessage = "resource_already_exists_exception"

  /**
    * create an ElasticSearch index if it doesn't exist.
    * @param indexName name of the index to create, matches the json file name under the index-mapping directory.
    * @param mappingName mapping name of the index, matches the index mapping attribute specified in the json file.
    * @return Either monad:
    *         in case of successfully created an index or the index already exists it returns the name of the index.
    *         in case of failure it returns the causing error.
    */
  def create(indexName: String, mappingName: String): Either[RequestFailure, String] = {

    val elasticClient = HttpClient(ElasticsearchClientUri(appConfig.ElasticHostName, appConfig.ElasticPort))
    val path = getClass.getClassLoader.getResourceAsStream(s"index-mapping/$indexName.json")
    val pattern = scala.io.Source.fromInputStream(path).mkString

    logger.info(s"creating index with name $indexName")

    val result = elasticClient.execute {
      createIndex(indexName).mappings(mapping(mappingName).rawSource(pattern))
    }.await

    elasticClient.close()

    result match {
      case Left(failure) =>
        if (failure.error.`type` == IndexAlreadyExistsErrorMessage) {
          logger.info(s"index with name $indexName already exists")
          Right(s"$indexName/$mappingName")
        } else {
          logger.error("failed to create index, reason: " + failure)
          Left(failure)
        }

      case Right(_) =>
        logger.info(s"index created successfully with name $indexName/$mappingName")
        Right(s"$indexName/$mappingName")
    }

  }

}
