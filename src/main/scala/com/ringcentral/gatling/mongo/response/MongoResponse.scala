package com.ringcentral.gatling.mongo.response

import reactivemongo.api.commands.BoxedAnyVal

trait MongoResponse

case class MongoStringResponse(response: String) extends MongoResponse

case class MongoCountResponse(count: Int) extends BoxedAnyVal[Int] with MongoResponse {
  def value = count
}
