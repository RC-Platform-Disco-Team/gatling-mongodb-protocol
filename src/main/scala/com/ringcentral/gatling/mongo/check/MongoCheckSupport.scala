package com.ringcentral.gatling.mongo.check

import com.ringcentral.gatling.mongo.check.body.MongoResponseJsonPathCheckBuilder
import com.ringcentral.gatling.mongo.check.count.MongoCountCheckBuilder
import io.gatling.core.check.extractor.jsonpath.JsonPathExtractorFactory
import io.gatling.core.json.JsonParsers
import io.gatling.core.session.Expression

trait MongoCheckSupport {
  val count = MongoCountCheckBuilder.count

  def jsonPath(path: Expression[String])(implicit extractorFactory: JsonPathExtractorFactory, jsonParsers: JsonParsers) =
    MongoResponseJsonPathCheckBuilder.jsonPath(path)
}
