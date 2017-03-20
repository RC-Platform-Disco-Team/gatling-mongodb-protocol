package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoRemoveCommand
import com.ringcentral.gatling.mongo.response.{MongoCountResponse, MongoStringResponse}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoRemoveCommandAction(command: MongoRemoveCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo find command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
    resolvedSelector <- command.selector(session)
    selector <- string2JsObject(resolvedSelector)
  } yield {
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)
    val sent = nowMillis
    collection.remove(selector).onComplete {
      case Success(result) => {
        val received = nowMillis
        if(result.ok) {
          processResult(session, sent, received, command.checks, MongoCountResponse(result.n), next, commandName)
        } else {
          executeNext(session, sent, received, KO, next, commandName, Some(result.writeErrors.map(we => s"[${we.code}] ${we.errmsg}").mkString(", ")))
        }
      }
      case Failure(err) => {
        val received = nowMillis
        executeNext(session, sent, received, KO, next, commandName, Some(err.getMessage))
      }
    }

  }
}