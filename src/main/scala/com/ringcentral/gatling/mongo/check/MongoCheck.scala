package com.ringcentral.gatling.mongo.check

import com.ringcentral.gatling.mongo.response.MongoResponse
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Session
import io.gatling.core.check.{Check, CheckResult}

import scala.collection.mutable

case class MongoCheck(wrapped: Check[MongoResponse]) extends Check[MongoResponse] {
  override def check(response: MongoResponse, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] =
    wrapped.check(response, session)
}
