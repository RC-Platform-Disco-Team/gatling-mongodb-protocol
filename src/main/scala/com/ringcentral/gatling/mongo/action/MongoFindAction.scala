package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoFindCommand
import com.ringcentral.gatling.mongo.response.MongoStringResponse
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session, resolveOptionalExpression}
import io.gatling.core.stats.StatsEngine
import play.api.libs.json.JsObject
import reactivemongo.api.{DefaultDB, QueryOpts, ReadPreference}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.collection.JsCursor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoFindAction(command: MongoFindCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo find command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] =
    for {
      collectionName <- command.collection(session)
      resolvedFilter <- command.query(session)
      filter <- string2JsObject(resolvedFilter)
      resolvedHint <- resolveOptionalExpression(command.hint, session)
      hint <- string2JsObject(resolvedHint)
      resolvedSort <- resolveOptionalExpression(command.sort, session)
      sort <- string2JsObject(resolvedSort)
    } yield {
      val sent = nowMillis
      database.collection[JSONCollection](collectionName).find(filter).options(QueryOpts().batchSize(command.limit)).sort(sort).hint(hint)
        .cursor[JsObject](ReadPreference.primary).jsArray(command.limit).onComplete {
        case Success(result) => processResult(session, sent, nowMillis, command.checks, MongoStringResponse(result.toString()), next, commandName)
        case Failure(err) => executeNext(session, sent, nowMillis, KO, next, commandName, Some(err.getMessage))
      }
    }
}

