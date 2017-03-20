package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoInsertCommand
import com.ringcentral.gatling.mongo.response.MongoCountResponse
import io.gatling.commons.stats.{KO, OK, Status}
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.ImplicitBSONHandlers._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class MongoInsertCommandAction(command: MongoInsertCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo insert command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
    resolvedDocument <- command.document(session)
    document <- string2JsObject(resolvedDocument)
  } yield {
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)
    val sent = nowMillis
    collection.insert(document).onComplete {
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