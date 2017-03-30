package com.ringcentral.gatling.mongo.check.count

import com.ringcentral.gatling.mongo.check.{MongoCheck, MongoCheckBuilders}
import com.ringcentral.gatling.mongo.response.{MongoCountResponse, MongoResponse}
import io.gatling.commons.validation._
import io.gatling.core.Predef._
import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.core.check.extractor.{Extractor, SingleArity}

object MongoCountCheckBuilder {

  private val countExtractor: Extractor[MongoResponse, Int] = new Extractor[MongoResponse, Int] with SingleArity {
    override def name: String = "count"

    override def apply(prepared: MongoResponse): Validation[Some[Int]] = prepared match {
      case response: MongoCountResponse => Some(response.value).success
      case _ => "Response wasn't received".failure
    }
  }

  val count = new DefaultFindCheckBuilder[MongoCheck, MongoResponse, MongoResponse, Int](MongoCheckBuilders.countExtender, MongoCheckBuilders.passThroughResponsePreparer, countExtractor)
}
