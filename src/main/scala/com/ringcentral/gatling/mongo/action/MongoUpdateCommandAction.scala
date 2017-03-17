package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoUpdateCommand
import io.gatling.commons.stats.{KO, OK, Status}
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

class MongoUpdateCommandAction(command: MongoUpdateCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo update command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
    resolvedSelector <- command.selector(session)
    resolvedModifier <- command.modifier(session)
    selector <- string2JsObject(resolvedSelector)
    modifier <- string2JsObject(resolvedModifier)
  } yield {
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)
    val startTime = nowMillis
    collection.update(selector, modifier).onComplete {
      case Success(result) => {
        val endTime = nowMillis
        val status: Status = if (result.ok) OK else KO
        val messageBuilder: StringBuilder = new StringBuilder()
        result.writeErrors.foreach(we => messageBuilder.append(s"[${we.code}] ${we.errmsg}"))
        statsEngine.logResponse(session, commandName, ResponseTimings(startTime, endTime), status, Option(result.code).map(_.toString), Some(messageBuilder.toString()))
        next ! session
      }
      case Failure(err) => {
        val endTime = nowMillis
        statsEngine.logResponse(session, commandName, ResponseTimings(startTime, endTime), KO, None, Some(err.getMessage))
        next ! session
      }
    }

  }
}