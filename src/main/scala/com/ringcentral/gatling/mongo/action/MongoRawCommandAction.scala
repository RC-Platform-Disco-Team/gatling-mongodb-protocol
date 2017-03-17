package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoRawCommand
import com.ringcentral.gatling.mongo.response.{MongoResponse, MongoStringResponse}
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import play.api.libs.json.JsObject
import reactivemongo.api.{DefaultDB, FailoverStrategy, ReadPreference}
import reactivemongo.api.commands.Command
import reactivemongo.play.json.JSONSerializationPack
import reactivemongo.play.json.ImplicitBSONHandlers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoRawCommandAction(command: MongoRawCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo raw command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    commandText <- command.command(session)
    commandDocument <- string2JsObject(commandText)
  } yield {
    val runner = Command.run(JSONSerializationPack, FailoverStrategy.default)
    val sent = nowMillis
    runner.apply(database, runner.rawCommand(commandDocument)).one[JsObject](ReadPreference.primaryPreferred).onComplete {
      case Success(result) =>
        val received = nowMillis
        processResult(session, sent, received, command.checks, MongoStringResponse(result.toString()), next, commandName)
      case Failure(err) =>
        val received = nowMillis
        executeNext(session, sent, received, KO, next, commandName, Some(err.getMessage))
    }
  }
}
