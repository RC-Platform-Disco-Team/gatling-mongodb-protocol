package com.ringcentral.gatling.mongo.check.body

import com.ringcentral.gatling.mongo.check.{MongoCheck, MongoCheckBuilders}
import com.ringcentral.gatling.mongo.response.{MongoResponse, MongoStringResponse}
import io.gatling.core.check.{DefaultMultipleFindCheckBuilder, Preparer}
import io.gatling.commons.validation._
import io.gatling.core.check.extractor.{CountArity, CriterionExtractor, FindAllArity, FindArity}
import io.gatling.core.check.extractor.jsonpath.{JsonFilter, JsonPathExtractorFactory}
import io.gatling.core.json.JsonParsers
import io.gatling.core.session.Expression

trait MongoResponseJsonPathOfType {
  self: MongoResponseJsonPathCheckBuilder[String] =>
  def ofType[X: JsonFilter](implicit extractorFactory: JsonPathExtractorFactory) = new MongoResponseJsonPathCheckBuilder[X](path, jsonParsers)
}

object MongoResponseJsonPathCheckBuilder {

  def preparer(jsonParsers: JsonParsers): Preparer[MongoResponse, Any] = {
    case response: MongoStringResponse => jsonParsers.safeParseBoon(response.response)
    case _ => "Response wasn't received".failure
  }

  def jsonPath(path: Expression[String])(implicit extractorFactory: JsonPathExtractorFactory, jsonParsers: JsonParsers) =
    new MongoResponseJsonPathCheckBuilder[String](path, jsonParsers) with MongoResponseJsonPathOfType
}

class MongoResponseJsonPathCheckBuilder[X: JsonFilter](
  private[body] val path: Expression[String],
  private[body] val jsonParsers: JsonParsers)
  (implicit extractorFactory: JsonPathExtractorFactory)
    extends DefaultMultipleFindCheckBuilder[MongoCheck, MongoResponse, Any, X](
      MongoCheckBuilders.bodyExtender,
      MongoResponseJsonPathCheckBuilder.preparer(jsonParsers)
    ) {

  import extractorFactory._

  def findExtractor(occurrence: Int): Expression[CriterionExtractor[Any, String, X] with FindArity] = path.map(newSingleExtractor[X](_, occurrence))

  def findAllExtractor: Expression[CriterionExtractor[Any, String, Seq[X]] with FindAllArity] = path.map(newMultipleExtractor[X])

  def countExtractor: Expression[CriterionExtractor[Any, String, Int] with CountArity] = path.map(newCountExtractor)
}
