package com.ringcentral.gatling.mongo

import com.ringcentral.gatling.mongo.action.MongoActionBuilder
import com.ringcentral.gatling.mongo.check.MongoCheckSupport
import com.ringcentral.gatling.mongo.command.{MongoCollectionCountCommandDslBuilder, MongoCommand, MongoCommandBuilder, MongoDslBuilderBase}
import com.ringcentral.gatling.mongo.protocol.{MongoProtocol, MongoProtocolBuilder, MongoProtocolBuilderBase, MongoProtocolUriBuilder}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

trait MongoDsl extends MongoCheckSupport {

  def mongo(implicit configuration: GatlingConfiguration) = MongoProtocolBuilderBase
  def mongo(requestName: Expression[String]) = MongoDslBuilderBase(requestName)

  implicit def mongoProtocolUriBuilder2mongoProtocol(builder: MongoProtocolUriBuilder): MongoProtocol = builder.build()
  implicit def mongoProtocolBuilder2mongoProtocol(builder: MongoProtocolBuilder): MongoProtocol = builder.build()
//  implicit def mongoCommandBuilder2MongoCommand(builder: MongoCommandBuilder): MongoCommand = builder.build()
//  implicit def mongoCollectionCountCommandDslBuilder2MongoCommand(builder: MongoCollectionCountCommandDslBuilder): MongoCommand = builder.build()


  implicit def mongoCommandBuilder2ActionBuilder(commandBuilder: MongoCommandBuilder)(implicit configuration: GatlingConfiguration): ActionBuilder = {
    new MongoActionBuilder(commandBuilder.build(), configuration)
  }

//  implicit def mongoCollectionCountCommandDslBuilder2ActionBuilder(commandBuilder: MongoCollectionCountCommandDslBuilder): ActionBuilder = {
//    new MongoActionBuilder(commandBuilder.build(), commandBuilder.configuration)
//  }


//  implicit def mongoCommand2ActionBuilder(command: MongoCommand):ActionBuilder = new MongoActionBuilder(command, null)
}
