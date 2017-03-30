package com.ringcentral.gatling.mongo.response

trait MongoResponse

case class MongoStringResponse(response: String) extends MongoResponse

case class MongoCountResponse(count: Int) extends MongoResponse {

  def value: Int = count
}
