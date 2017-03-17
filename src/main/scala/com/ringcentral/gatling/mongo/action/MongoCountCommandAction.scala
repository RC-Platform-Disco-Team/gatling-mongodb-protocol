package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.command.MongoCountCommand
import com.ringcentral.gatling.mongo.response.MongoCountResponse
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MongoCountCommandAction(command: MongoCountCommand, database: DefaultDB, val statsEngine: StatsEngine, configuration: GatlingConfiguration, val next: Action) extends MongoAction(database) {

  override def name: String = genName("Mongo count command")

  override def commandName: Expression[String] = command.commandName

  override def executeCommand(commandName: String, session: Session): Validation[Unit] = for {
    collectionName <- command.collection(session)
  } yield {
    val collection: JSONCollection = database.collection[JSONCollection](collectionName)
    val sent = nowMillis
    collection.count().onComplete {
      case Success(result) => {
        val received = nowMillis
        processResult(session, sent, received, command.checks, MongoCountResponse(result), next, commandName)
      }
      case Failure(err) => {
        val received = nowMillis
        executeNext(session, sent, received, KO, next, commandName, Some(err.getMessage))
      }
    }

  }
}