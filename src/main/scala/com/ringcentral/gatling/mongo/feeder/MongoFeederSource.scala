package com.ringcentral.gatling.mongo.feeder

import com.ringcentral.gatling.mongo.MongoUtils
import io.gatling.core.feeder.Record
import play.api.libs.iteratee._
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MongoFeederSource {

  val defaultPostProcessor: JsObject => Map[String, Any] = o => o.fields.toMap

  def apply(url: String, collectionName: String, query: String, limit: Int, batchSize: Int, connectionTimeout: FiniteDuration, receiveTimeout: FiniteDuration, postProcessor: JsObject => Map[String, Any]): Vector[Record[Any]] = {
    Await.result(run(url, collectionName, query, limit, batchSize, connectionTimeout, postProcessor), receiveTimeout)
  }


  private def run(url: String, collectionName: String, query: String, limit: Int, batchSize: Int, connectionTimeout: FiniteDuration, postProcessor: (JsObject) => Map[String, Any]): Future[Vector[Record[Any]]] = {
    val document = Json.parse(query).as[JsObject]
    val collection: JSONCollection = MongoUtils.connectToDB(url, connectionTimeout).collection[JSONCollection](collectionName)
    val resultSet: Enumerator[Map[String, Any]] = collection.find(document).options(QueryOpts().batchSize(batchSize)).cursor[JsObject](ReadPreference.primary).enumerator(limit).map(postProcessor)

    resultSet.run(Iteratee.fold(Vector.empty[Record[Any]]) { (acc, next) => acc :+ next })
  }
}
