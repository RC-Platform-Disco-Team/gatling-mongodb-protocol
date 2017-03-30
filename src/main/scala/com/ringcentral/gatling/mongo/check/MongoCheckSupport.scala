package com.ringcentral.gatling.mongo.check

import com.ringcentral.gatling.mongo.check.body.{MongoResponseJsonPathCheckBuilder, MongoResponseJsonPathOfType}
import com.ringcentral.gatling.mongo.check.count.MongoCountCheckBuilder
import com.ringcentral.gatling.mongo.response.MongoResponse
import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.core.check.extractor.jsonpath.JsonPathExtractorFactory
import io.gatling.core.json.JsonParsers
import io.gatling.core.session.Expression

trait MongoCheckSupport {

  val count: DefaultFindCheckBuilder[MongoCheck, MongoResponse, MongoResponse, Int] = MongoCountCheckBuilder.count

  def jsonPath(path: Expression[String])(implicit extractorFactory: JsonPathExtractorFactory, jsonParsers: JsonParsers): MongoResponseJsonPathCheckBuilder[String] with MongoResponseJsonPathOfType =
    MongoResponseJsonPathCheckBuilder.jsonPath(path)
}
