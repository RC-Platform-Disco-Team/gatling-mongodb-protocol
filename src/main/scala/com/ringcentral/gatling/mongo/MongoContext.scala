package com.ringcentral.gatling.mongo

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

trait MongoContext {
  def database: DefaultDB
}

object MongoContext extends StrictLogging {

  case class MongoDatabaseContext(database: DefaultDB) extends MongoContext

  private val driver = new MongoDriver

  def apply(uri: ParsedURI, connectionTimeout: FiniteDuration): MongoContext = {
    val connection: MongoConnection = driver.connection(uri)

    val database = uri.db match {
      case Some(dbName) => Try(Await.result(connection.database(dbName), connectionTimeout)) match {
        case Success(db) => db
        case Failure(err) =>
          throw new IllegalStateException(s"Can't connect to database ${uri.hosts.map(item=>s"${item._1}:${item._2}").mkString(", ")}. $err")
      }
      case None => throw new IllegalStateException(s"Can't connect to database $uri.")
    }
    MongoDatabaseContext(database)
  }
}