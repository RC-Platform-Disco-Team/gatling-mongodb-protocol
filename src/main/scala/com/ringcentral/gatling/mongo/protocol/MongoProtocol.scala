package com.ringcentral.gatling.mongo.protocol

import akka.actor.ActorSystem
import com.ringcentral.gatling.mongo.MongoContext
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

object MongoProtocol {
  val MongoProtocolKey = new ProtocolKey {
    type Protocol = MongoProtocol
    type Components = MongoComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[MongoProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultValue(configuration: GatlingConfiguration): MongoProtocol = throw new IllegalStateException("Can't provide a default value for JmsProtocol")

    def newComponents(system: ActorSystem, coreComponents: CoreComponents): MongoProtocol => MongoComponents = {
          mongoProtocol => MongoComponents(
            mongoProtocol,
            MongoContext.apply(mongoProtocol.uri, mongoProtocol.connectionTimeout, mongoProtocol.nbChannelsPerNode))
    }
  }
}

case class MongoProtocol(uri: String, connectionTimeout: FiniteDuration = 5 seconds, nbChannelsPerNode: Option[Int]) extends Protocol

case object MongoProtocolBuilderBase {
  def uri(uri: String) = MongoProtocolBuilder(uri)
}

case class MongoProtocolBuilder(uri: String, connectionTimeout: FiniteDuration = 5 seconds, nbChannelsPerNode: Option[Int] = None) {

  def uri(uri: String):MongoProtocolBuilder  = copy(uri=uri)
  def connectionTimeout(connectionTimeout: FiniteDuration): MongoProtocolBuilder = copy(connectionTimeout=connectionTimeout)
  def nbChannelsPerNode(nbChannelsPerNode: Int): MongoProtocolBuilder = copy(nbChannelsPerNode=Some(nbChannelsPerNode))

	def build() = new MongoProtocol(uri, connectionTimeout, nbChannelsPerNode)
}