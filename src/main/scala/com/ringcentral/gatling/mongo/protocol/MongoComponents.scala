package com.ringcentral.gatling.mongo.protocol

import com.ringcentral.gatling.mongo.MongoContext
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class MongoComponents(mongoProtocol: MongoProtocol, mongoContext: MongoContext) extends ProtocolComponents with StrictLogging {

  override def onStart: Option[(Session) => Session] = None

  override def onExit: Option[(Session) => Unit] = None
}
