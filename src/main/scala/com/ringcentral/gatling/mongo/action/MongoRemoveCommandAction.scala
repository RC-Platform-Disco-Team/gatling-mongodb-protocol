package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoRemoveCommand
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
    val startTime = nowMillis
    collection.remove(selector).onComplete {
      case Success(result) => {
        val endTime = nowMillis
        statsEngine.logResponse(session, commandName, ResponseTimings(startTime, endTime), OK, None, None)
        next ! session
      }
      case Failure(message) => {
        val endTime = nowMillis
        statsEngine.logResponse(session, commandName, ResponseTimings(startTime, endTime), KO, None, Some(message.getMessage))
        next ! session
      }
    }

  }
}