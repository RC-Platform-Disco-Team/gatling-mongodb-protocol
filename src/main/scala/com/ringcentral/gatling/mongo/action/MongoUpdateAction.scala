package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoUpdateCommand
import com.ringcentral.gatling.mongo.response.MongoCountResponse
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoUpdateAction(command: MongoUpdateCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo update command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
    resolvedSelector <- command.selector(session)
    resolvedModifier <- command.modifier(session)
    selector <- string2JsObject(resolvedSelector)
    modifier <- string2JsObject(resolvedModifier)
  } yield {
    val sent = nowMillis
    database.collection[JSONCollection](collectionName).update(selector, modifier).onComplete {
      case Success(result) =>
        if (result.ok) {
          processResult(session, sent, nowMillis, command.checks, MongoCountResponse(result.n), next, commandName)
        } else {
          executeNext(session, sent, nowMillis, KO, next, commandName, Some(result.writeErrors.map(we => s"[${we.code}] ${we.errmsg}").mkString(", ")))
        }
      case Failure(err) =>
        executeNext(session, sent, nowMillis, KO, next, commandName, Some(err.getMessage))
    }

  }
}