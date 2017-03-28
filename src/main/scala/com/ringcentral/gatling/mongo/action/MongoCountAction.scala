package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoCountCommand
import com.ringcentral.gatling.mongo.response.MongoCountResponse
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation._
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session, _}
import io.gatling.core.stats.StatsEngine
import play.api.libs.json.JsObject
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoCountAction(command: MongoCountCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo count command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] =
    for {
      collectionName <- command.collection(session)
      selectorDocument <- resolveOptionalExpression(command.selector, session)
      hint <- resolveOptionalExpression(command.hint, session)
      selector: Option[JsObject] <- selectorDocument.getOrElse(NoneSuccess)
    } yield {
      val sent = nowMillis
      database.collection[JSONCollection](collectionName).count(selector, command.limit, command.skip, hint).onComplete {
        case Success(result) => processResult(session, sent, nowMillis, command.checks, MongoCountResponse(result), next, commandName)
        case Failure(err) => executeNext(session, sent, nowMillis, KO, next, commandName, Some(err.getMessage))
      }

    }
}