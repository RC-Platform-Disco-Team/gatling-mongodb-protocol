package com.ringcentral.gatling.mongo.protocol

import akka.actor.ActorSystem
import com.ringcentral.gatling.mongo.MongoContext
import com.ringcentral.gatling.mongo.MongoContext.logger
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import reactivemongo.api.MongoConnection.{ParsedURI, URIParsingException}
import reactivemongo.api.commands.WriteConcern
import reactivemongo.api._
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object MongoProtocol {
  val MongoProtocolKey = new ProtocolKey {
    type Protocol = MongoProtocol
    type Components = MongoComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[MongoProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultValue(configuration: GatlingConfiguration): MongoProtocol = throw new IllegalStateException("Can't provide a default value for JmsProtocol")

    def newComponents(system: ActorSystem, coreComponents: CoreComponents): MongoProtocol => MongoComponents = {
          mongoProtocol => MongoComponents(
            mongoProtocol,
            MongoContext.apply(mongoProtocol.uri, mongoProtocol.connectionTimeout))
    }
  }
}

case class MongoProtocol(uri: ParsedURI, connectionTimeout: FiniteDuration) extends Protocol

case object MongoProtocolBuilderBase {
  val DefaultPort = 27017

  def uri(uri: String) = MongoProtocolUriBuilder(uri)

  def hosts(hosts: String*) = MongoProtocolBuilder(parseHosts(hosts.mkString(",")))

  private def parseHosts(hosts: String) = hosts.split(",").toList.map { host =>
    host.split(':').toList match {
      case host :: port :: Nil => host -> {
        try {
          val p = port.toInt
          if (p > 0 && p < 65536) p
          else throw new URIParsingException(s"Could not parse hosts '$hosts' from URI: invalid port '$port'")
        } catch {
          case _: NumberFormatException => throw new URIParsingException(s"Could not parse hosts '$hosts' from URI: invalid port '$port'")
          case NonFatal(e)              => throw e
        }
      }
      case host :: Nil => host -> DefaultPort
      case _           => throw new URIParsingException(s"Could not parse hosts from URI: invalid definition '$hosts'")
    }
  }
}

case class MongoProtocolUriBuilder(uri: String, connectionTimeout: FiniteDuration = 5 seconds) extends StrictLogging {

  def uri(uri: String): MongoProtocolUriBuilder  = copy(uri=uri)
  def connectionTimeout(connectionTimeout: FiniteDuration): MongoProtocolUriBuilder = copy(connectionTimeout=connectionTimeout)

	def build() = {
    MongoConnection.parseURI(uri) match {
      case Success(parsedUri) =>
        logger.debug(s"Successful parsed mongo uri '$uri'.")
        new MongoProtocol(parsedUri, connectionTimeout)
      case Failure(err) => throw new IllegalStateException(s"Can't parse database uri. $err")
    }
  }
}

case class MongoProtocolBuilder(
  hosts: List[(String, Int)],
  options: MongoConnectionOptions = MongoConnectionOptions(),
  db: Option[String] = None,
  authenticate: Option[Authenticate] = None,
  connectionTimeout: FiniteDuration = 5 seconds) {

  def connectTimeoutMS(connectTimeoutMS: Int): MongoProtocolBuilder =
    copy(connectionTimeout = Duration.create(connectTimeoutMS.toLong, MILLISECONDS),
         options=options.copy(connectTimeoutMS=connectTimeoutMS))

  // canonical options - authentication options
  def authSource(authSource: String): MongoProtocolBuilder = copy(options=options.copy(authSource=Some(authSource)))
  def sslEnabled(sslEnabled: Boolean): MongoProtocolBuilder = copy(options=options.copy(sslEnabled=sslEnabled))
  def sslAllowsInvalidCert(sslAllowsInvalidCert: Boolean): MongoProtocolBuilder = copy(options=options.copy(sslAllowsInvalidCert=sslAllowsInvalidCert))
  def authMode(authMode: AuthenticationMode): MongoProtocolBuilder = copy(options=options.copy(authMode=authMode))

  // reactivemongo specific options
  def tcpNoDelay(tcpNoDelay: Boolean): MongoProtocolBuilder = copy(options=options.copy(tcpNoDelay=tcpNoDelay))
  def keepAlive(keepAlive: Boolean): MongoProtocolBuilder = copy(options=options.copy(keepAlive=keepAlive))
  def nbChannelsPerNode(nbChannelsPerNode: Int): MongoProtocolBuilder = copy(options=options.copy(nbChannelsPerNode=nbChannelsPerNode))

  // read and write preferences
  def writeConcern(writeConcern: WriteConcern): MongoProtocolBuilder = copy(options=options.copy(writeConcern=writeConcern))
  def readPreference(readPreference: ReadPreference): MongoProtocolBuilder = copy(options=options.copy(readPreference=readPreference))

  def failoverStrategy(failoverStrategy: FailoverStrategy): MongoProtocolBuilder = copy(options=options.copy(failoverStrategy=failoverStrategy))

  def monitorRefreshMS(monitorRefreshMS: Int): MongoProtocolBuilder = copy(options=options.copy(monitorRefreshMS=monitorRefreshMS))
  def maxIdleTimeMS(maxIdleTimeMS: Int): MongoProtocolBuilder = copy(options=options.copy(maxIdleTimeMS=maxIdleTimeMS))

  def database(database: String): MongoProtocolBuilder = authenticate match {
    case Some(auth) => copy(db=Some(database), authenticate=Some(auth.copy(db = database)))
    case None       => copy(db=Some(database))
  }

  def username(username: String): MongoProtocolBuilder = {
    copy(authenticate=Some(authenticate match {
      case Some(auth) => auth.copy(user = username)
      case None       => Authenticate.apply(options.authSource.getOrElse(db.getOrElse("")), username, "")
    }))}

  def password(password: String): MongoProtocolBuilder = {
    copy(authenticate=Some(authenticate match {
      case Some(auth) => auth.copy(password = password)
      case None       => Authenticate.apply(options.authSource.getOrElse(db.getOrElse("")), "", password)
    }))}

  def build() = new MongoProtocol(ParsedURI(hosts, options, List[String](), db, authenticate), connectionTimeout)

}