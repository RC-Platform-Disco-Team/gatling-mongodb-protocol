package com.ringcentral.gatling.mongo.protocol

import com.ringcentral.gatling.mongo.MongoUtils
import io.gatling.core.protocol.Protocol
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api._
import reactivemongo.api.commands.WriteConcern
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.duration.{FiniteDuration, _}

/**
  * Complimentary object for MongoDB protocol
  */
object MongoProtocol {

  /**
    * Creates builder to build [[MongoProtocol]] from connection string
    *
    * @param uri connection string
    * @return builder instance
    */
  def uri(uri: String) = MongoProtocolUriBuilder(uri)

  /**
    * Creates builder to build [[MongoProtocol]] from parameters
    *
    * @param hosts list of hosts to connect
    * @return builder instance
    */
  def hosts(hosts: String*) = MongoProtocolFieldsBuilder(MongoUtils.parseHosts(hosts))

}

case class MongoProtocol(private[protocol] val uri: ParsedURI, private[protocol] val connectionTimeout: FiniteDuration) extends Protocol

/**
  * Builder class to build [[MongoProtocol]] from connection string
  *
  * @param uri               connection string
  * @param connectionTimeout timeout value
  */
case class MongoProtocolUriBuilder(private val uri: String,
                                   private val connectionTimeout: FiniteDuration = 5 seconds) {

  def uri(uri: String): MongoProtocolUriBuilder = copy(uri = uri)

  def connectionTimeout(connectionTimeout: FiniteDuration): MongoProtocolUriBuilder = copy(connectionTimeout = connectionTimeout)

  def build(): MongoProtocol = MongoProtocol(MongoUtils.parseUri(uri), connectionTimeout)
}

case class MongoProtocolFieldsBuilder(hosts: Seq[(String, Int)] = List.empty,
                                      options: MongoConnectionOptions = MongoConnectionOptions(),
                                      db: Option[String] = None,
                                      authenticate: Option[Authenticate] = None,
                                      connectionTimeout: FiniteDuration = 5 seconds) {

  def connectTimeout(connectTimeout: FiniteDuration): MongoProtocolFieldsBuilder = copy(connectionTimeout = connectTimeout)

  // canonical options - authentication options
  def authSource(authSource: String): MongoProtocolFieldsBuilder = copy(options = options.copy(authSource = Some(authSource)))

  def sslEnabled(sslEnabled: Boolean): MongoProtocolFieldsBuilder = copy(options = options.copy(sslEnabled = sslEnabled))

  def sslAllowsInvalidCert(sslAllowsInvalidCert: Boolean): MongoProtocolFieldsBuilder = copy(options = options.copy(sslAllowsInvalidCert = sslAllowsInvalidCert))

  def authMode(authMode: AuthenticationMode): MongoProtocolFieldsBuilder = copy(options = options.copy(authMode = authMode))

  // reactivemongo specific options
  def tcpNoDelay(tcpNoDelay: Boolean): MongoProtocolFieldsBuilder = copy(options = options.copy(tcpNoDelay = tcpNoDelay))

  def keepAlive(keepAlive: Boolean): MongoProtocolFieldsBuilder = copy(options = options.copy(keepAlive = keepAlive))

  def nbChannelsPerNode(nbChannelsPerNode: Int): MongoProtocolFieldsBuilder = copy(options = options.copy(nbChannelsPerNode = nbChannelsPerNode))

  // read and write preferences
  def writeConcern(writeConcern: WriteConcern): MongoProtocolFieldsBuilder = copy(options = options.copy(writeConcern = writeConcern))

  def readPreference(readPreference: ReadPreference): MongoProtocolFieldsBuilder = copy(options = options.copy(readPreference = readPreference))

  def failoverStrategy(failoverStrategy: FailoverStrategy): MongoProtocolFieldsBuilder = copy(options = options.copy(failoverStrategy = failoverStrategy))

  def monitorRefreshMS(monitorRefreshMS: Int): MongoProtocolFieldsBuilder = copy(options = options.copy(monitorRefreshMS = monitorRefreshMS))

  def maxIdleTimeMS(maxIdleTimeMS: Int): MongoProtocolFieldsBuilder = copy(options = options.copy(maxIdleTimeMS = maxIdleTimeMS))

  def database(database: String): MongoProtocolFieldsBuilder = authenticate match {
    case Some(auth) => copy(db = Some(database), authenticate = Some(auth.copy(db = database)))
    case None => copy(db = Some(database))
  }

  def username(username: String): MongoProtocolFieldsBuilder = {
    copy(authenticate = Some(authenticate match {
      case Some(auth) => auth.copy(user = username)
      case None => Authenticate.apply(options.authSource.getOrElse(db.getOrElse("")), username, "")
    }))
  }

  def password(password: String): MongoProtocolFieldsBuilder = {
    copy(authenticate = Some(authenticate match {
      case Some(auth) => auth.copy(password = password)
      case None => Authenticate.apply(options.authSource.getOrElse(db.getOrElse("")), "", password)
    }))
  }

  def build() = new MongoProtocol(ParsedURI(hosts.toList, options, List.empty, db, authenticate), connectionTimeout)

}