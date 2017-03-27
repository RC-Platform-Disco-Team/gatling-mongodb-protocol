package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoFindCommand
import com.ringcentral.gatling.mongo.response.MongoStringResponse
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.commons.validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session, resolveOptionalExpression}
import io.gatling.core.stats.StatsEngine
import play.api.libs.json.JsObject
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.{DefaultDB, QueryOpts, ReadPreference}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.JSONSerializationPack
import reactivemongo.play.json.collection.JsCursor._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoFindCommandAction(command: MongoFindCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo find command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
    resolvedFilter <- command.query(session)
    filter <- string2JsObject(resolvedFilter)
    resolvedHint <- resolveOptionalExpression(command.hint, session)
    hint <- string2JsObject(resolvedHint)
    resolvedSort <- resolveOptionalExpression(command.sort, session)
    sort <- string2JsObject(resolvedSort)

  } yield {
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)

//    var queryBuilder = collection.find(filter).options(QueryOpts().batchSize(command.limit))

    val queryBuilder = (sort, hint) match {
      case (Some(sortDocument), Some(hintDocument)) => collection.find(filter).options(QueryOpts().batchSize(command.limit)).sort(sortDocument).hint(hintDocument)
      case (Some(document), None)                   => collection.find(filter).options(QueryOpts().batchSize(command.limit)).sort(document)
      case (None, Some(document))                   => collection.find(filter).options(QueryOpts().batchSize(command.limit)).hint(document)
      case _                                        => collection.find(filter).options(QueryOpts().batchSize(command.limit))
    }

    val sent = nowMillis
    queryBuilder.cursor[JsObject](ReadPreference.primary).jsArray(command.limit).onComplete {
      case Success(result) => {
        val received = nowMillis
        processResult(session, sent, received, command.checks, MongoStringResponse(result.toString()), next, commandName)
      }
      case Failure(err) => {
        val received = nowMillis
        executeNext(session, sent, received, KO, next, commandName, Some(err.getMessage))
      }
    }

  }
}