package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command._
import com.ringcentral.gatling.mongo.protocol.{MongoComponents, MongoProtocol}
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.structure.ScenarioContext

class MongoActionBuilder(command: MongoCommand, configuration: GatlingConfiguration) extends ActionBuilder {

  protected def mongoComponents(ctx: ScenarioContext): MongoComponents = {
    ctx.protocolComponentsRegistry.components(MongoProtocol.mongoProtocolKey)
  }

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val components = mongoComponents(ctx)
    val databaseContext = components.mongoContext
    command match {
      case rawCommand: MongoRawCommand => new MongoRawCommandAction(rawCommand, databaseContext.database, statsEngine, configuration, next)
      case countCommand: MongoCountCommand => new MongoCountCommandAction(countCommand, databaseContext.database, statsEngine, configuration, next)
      case findCommand: MongoFindCommand =>  new MongoFindCommandAction(findCommand, databaseContext.database, statsEngine, configuration, next)
      case removeCommand: MongoRemoveCommand =>  new MongoRemoveCommandAction(removeCommand, databaseContext.database, statsEngine, configuration, next)
      case insertCommand: MongoInsertCommand =>  new MongoInsertCommandAction(insertCommand, databaseContext.database, statsEngine, configuration, next)
      case updateCommand: MongoUpdateCommand =>  new MongoUpdateCommandAction(updateCommand, databaseContext.database, statsEngine, configuration, next)
    }

  }
}