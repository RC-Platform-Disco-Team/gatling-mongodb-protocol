package com.ringcentral.gatling.mongo.check

import com.ringcentral.gatling.mongo.response.{MongoCountResponse, MongoResponse}
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session

import scala.collection.mutable

//case class MongoSimpleCheck(func: MongoResponse => Boolean) extends MongoCheck {
//  override def check(response: MongoResponse, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
//    func(response) match {
//      case true => CheckResult.NoopCheckResultSuccess
//      case _    => Failure("Mongo check failed")
//    }
//  }
//}
//
//
//case class MongoCountCheck(expectedCount: Int) extends MongoCheck {
//  override def check(response: MongoResponse, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
//    response match {
//      case r: MongoCountResponse => if (r.value == expectedCount) {
//        Success(CheckResult(Some(r.value), Some("count")))
//        } else {
//          Failure(s"Count check failed. Expected $expectedCount, but actual is ${r.value}.")
//        }
//      case _ => CheckResult.NoopCheckResultSuccess
//    }
//
//  }
//}