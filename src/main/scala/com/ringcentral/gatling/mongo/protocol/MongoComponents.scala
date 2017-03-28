package com.ringcentral.gatling.mongo.protocol

import akka.actor.ActorSystem
import com.ringcentral.gatling.mongo.MongoContext
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

/**
  * Key for MongoDB protocol
  */
object MongoProtocolKey extends ProtocolKey {

  type Protocol = MongoProtocol
  type Components = MongoComponents

  def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[MongoProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

  def defaultValue(configuration: GatlingConfiguration): MongoProtocol = throw new IllegalStateException("Can't provide a default value for MongoProtocol")

  def newComponents(system: ActorSystem, coreComponents: CoreComponents): MongoProtocol => MongoComponents =
    mongoProtocol => MongoComponents(mongoProtocol, MongoContext(mongoProtocol.uri, mongoProtocol.connectionTimeout))
}

case class MongoComponents(mongoProtocol: MongoProtocol, mongoContext: MongoContext) extends ProtocolComponents {

  override def onStart: Option[(Session) => Session] = None

  override def onExit: Option[(Session) => Unit] = None
}
