package com.ringcentral.gatling.mongo

import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{DefaultDB, MongoDriver}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

/**
  * Context class with MongoDB connection
  *
  * @param database instance of connected MongoDB
  */
case class MongoContext(database: DefaultDB)

/**
  * Factory object for MongoDB context from uri
  */
object MongoContext {

  /**
    * Try to parse URI and establish connection. In case of failure or timeout throws [[IllegalStateException]]
    *
    * @param uri connection string to MongoDB
    * @param connectionTimeout timeout value
    * @return context with connected [[DefaultDB]] instance inside
    */
  def apply(uri: ParsedURI, connectionTimeout: FiniteDuration): MongoContext = MongoContext(MongoUtils.connectToDB(uri, connectionTimeout))

}