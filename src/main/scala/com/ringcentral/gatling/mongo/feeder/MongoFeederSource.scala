package com.ringcentral.gatling.mongo.feeder

import io.gatling.core.feeder.Record
import play.api.libs.iteratee._
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{MongoConnection, MongoDriver, QueryOpts, ReadPreference}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.iteratees.cursorProducer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object MongoFeederSource {

  val defaultPostProcessor: JsObject => Map[String, Any] = o => o.fields.toMap
  private val driver = new MongoDriver

  def apply(url: String, collectionName: String, query: String, limit: Int, batchSize: Int, connectionTimeout: FiniteDuration, receiveTimeout: FiniteDuration, postProcessor: JsObject => Map[String, Any]): Vector[Record[Any]] = {

    val uri: ParsedURI = MongoConnection.parseURI(url) match {
      case Success(parsedUri) => parsedUri
      case Failure(err) => throw new IllegalStateException(s"Can't parse database uri. $err")
    }

    val connection = driver.connection(uri)
    val database = uri.db match {
      case Some(dbName) => Try(Await.result(connection.database(dbName), connectionTimeout)) match {
        case Success(db) => db
        case Failure(err) =>
          throw new IllegalStateException(s"Can't connect to database $url. $err")
      }
      case None => throw new IllegalStateException(s"Can't connect to database $uri.")
    }
    val document = Json.parse(query).as[JsObject]
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)
    val resultSet: Enumerator[Map[String, Any]] = collection.find(document).options(QueryOpts().batchSize(batchSize)).cursor[JsObject](ReadPreference.primary).enumerator(limit).map(postProcessor)

    Await.result(resultSet.run(Iteratee.fold(Vector.empty[Record[Any]]) { (acc, next) => acc :+ next }), receiveTimeout)
  }


}
