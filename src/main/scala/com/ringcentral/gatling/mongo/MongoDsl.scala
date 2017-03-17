package com.ringcentral.gatling.mongo

import com.ringcentral.gatling.mongo.check.MongoCheckSupport
import com.ringcentral.gatling.mongo.command.MongoDslBuilderBase
import com.ringcentral.gatling.mongo.protocol.{MongoProtocol, MongoProtocolBuilder, MongoProtocolBuilderBase}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

trait MongoDsl extends MongoCheckSupport {

  def mongo(implicit configuration: GatlingConfiguration) = MongoProtocolBuilderBase
  def mongo(requestName: Expression[String]) = MongoDslBuilderBase(requestName)

  implicit def mongoProtocolBuilder2mongoProtocol(builder: MongoProtocolBuilder): MongoProtocol = builder.build()
}
