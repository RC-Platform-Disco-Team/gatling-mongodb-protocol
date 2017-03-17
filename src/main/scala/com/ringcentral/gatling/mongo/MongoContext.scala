package com.ringcentral.gatling.mongo

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait MongoContext {
  def database: DefaultDB
}

object MongoContext extends StrictLogging {

  case class MongoDatabaseContext(database: DefaultDB) extends MongoContext

  private val driver = new MongoDriver

  def apply(uri: String, connectionTimeout: FiniteDuration, nbChannelsPerNode: Option[Int]): MongoContext = {
    val mongoUri: ParsedURI = MongoConnection.parseURI(uri) match {
      case Success(parsedUri) =>
        logger.debug(s"Successful parsed mongo uri '$uri'.")
        val connectionOptions = nbChannelsPerNode match {
          case Some(channelsPerNode) => parsedUri.options.copy(nbChannelsPerNode = channelsPerNode)
          case None => parsedUri.options
        }
        parsedUri.copy(options = connectionOptions)
      case Failure(err) => throw new IllegalStateException(s"Can't parse database uri. $err")
    }

    val connection: MongoConnection = driver.connection(mongoUri)
    val database = mongoUri.db match {
      case Some(dbName) => Await.result(connection.database(dbName), connectionTimeout)
      case None => throw new IllegalStateException(s"Can't connect to database $uri.")
    }
    new MongoDatabaseContext(database)
  }
}