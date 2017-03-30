package com.ringcentral.gatling.mongo

import reactivemongo.api.MongoConnection.{ParsedURI, URIParsingException}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

// fixme remove global context
import scala.concurrent.ExecutionContext.Implicits.global

object MongoUtils {

  private val DEFAULT_PORT: Int = 27017
  private lazy val mongoDriver = new MongoDriver()

  private def establishConnection(uri: ParsedURI, dbName: String, connectionTimeout: FiniteDuration): DefaultDB = {
    Await.result(establishConnection(uri, dbName), connectionTimeout)
  }

  private def establishConnection(uri: ParsedURI, dbName: String): Future[DefaultDB] =
    Try(mongoDriver.connection(uri).database(dbName))
    match {
      case Success(db) => db
      case Failure(err) =>
        throw new IllegalStateException(s"Can't connect to database ${printHosts(uri.hosts)}: ${err.getMessage}", err)
    }

  private def printHosts(hosts: List[(String, Int)]): String = hosts.map(item => s"${item._1}:${item._2}").mkString(", ")

  def connectToDB(uri: ParsedURI, connectionTimeout: FiniteDuration): DefaultDB =
    uri.db match {
      case Some(dbName) => establishConnection(uri, dbName, connectionTimeout)
      case None => throw new IllegalStateException(s"Can't connect to database $uri.")
    }

  def connectToDB(uri: String, connectionTimeout: FiniteDuration): DefaultDB =  connectToDB(parseUri(uri), connectionTimeout)

  def parseHosts(hosts: Seq[String]): Seq[(String, Int)] = hosts.map { hostAndPort =>
    hostAndPort.split(':').toList match {
      case host :: port :: Nil =>
        host -> Try(port.toInt).filter(p => p > 0 && p < 65536)
          .getOrElse(throw new URIParsingException(s"Could not parse hosts '$hosts' from URI: invalid port '$port'"))
      case host :: Nil =>
        host -> DEFAULT_PORT
      case _ => throw new URIParsingException(s"Could not parse hosts from URI: invalid definition '$hosts'")
    }
  }

  def parseUri(uri: String): ParsedURI = {
    MongoConnection.parseURI(uri) match {
      case Success(parsedUri) => parsedUri
      case Failure(err) => throw new IllegalStateException(s"Can't parse database uri. $err")
    }
  }
}
