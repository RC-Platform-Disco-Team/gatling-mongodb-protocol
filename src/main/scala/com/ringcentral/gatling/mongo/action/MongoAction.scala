package com.ringcentral.gatling.mongo.action

import com.ringcentral.gatling.mongo.check.MongoCheck
import com.ringcentral.gatling.mongo.response.MongoResponse
import io.gatling.commons.stats.{KO, OK, Status}
import io.gatling.commons.validation
import io.gatling.commons.validation.{NoneSuccess, Validation}
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import play.api.libs.json._
import reactivemongo.api.DefaultDB

import scala.util.{Failure, Success, Try}

abstract class MongoAction(database: DefaultDB) extends ExitableAction with MongoLogging with NameGen {
  def commandName: Expression[String]
  def executeCommand(commandName: String, session: Session): Validation[Unit]

  override def execute(session: Session): Unit = recover(session) {
    commandName(session).flatMap { resolvedCommandName =>
      val outcome = executeCommand(resolvedCommandName, session)
      outcome.onFailure(errorMessage => statsEngine.reportUnbuildableRequest(session, resolvedCommandName, errorMessage))
      outcome
    }
  }

  def string2JsObject(string: String): Validation[JsObject] = {
    Try[JsObject](Json.parse(string).as[JsObject]) match {
      case Success(json) => validation.SuccessWrapper(json).success
      case Failure(err) =>
        validation.FailureWrapper(s"Error parse JSON string: $string. ${err.getMessage}").failure
    }
  }

  def string2JsObject(optionString: Option[String]): Validation[Option[JsObject]] = {
    optionString match {
      case Some(string) => string2JsObject(string).map(Some.apply)
      case None => NoneSuccess
    }
  }

  protected def executeNext(
    session:     Session,
    sent:        Long,
    received:    Long,
    status:      Status,
    next:        Action,
    requestName: String,
    message:     Option[String]
    ) = {
    val timings = ResponseTimings(sent, received)
    statsEngine.logResponse(session, requestName, timings, status, None, message)
    next ! session
  }

  protected def processResult(
    session:     Session,
    sent:        Long,
    received:    Long,
    checks:      List[MongoCheck],
    response:    MongoResponse,
    next:        Action,
    requestName: String
    ): Unit = {
    // run all the checks, advise the Gatling API that it is complete and move to next
    val (checkSaveUpdate, error) = Check.check(response, session, checks)
    val newSession = checkSaveUpdate(session)
    error match {
      case Some(validation.Failure(errorMessage)) => executeNext(newSession.markAsFailed, sent, received, KO, next, requestName, Some(errorMessage))
      case _                                      => executeNext(newSession, sent, received, OK, next, requestName, None)
    }
  }

}
