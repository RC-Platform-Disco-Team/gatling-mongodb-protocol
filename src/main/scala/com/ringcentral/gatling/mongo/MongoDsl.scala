package com.ringcentral.gatling.mongo

import com.ringcentral.gatling.mongo.action.MongoActionBuilder
import com.ringcentral.gatling.mongo.check.MongoCheckSupport
import com.ringcentral.gatling.mongo.command.{MongoCommandBuilder, MongoDslBuilder}
import com.ringcentral.gatling.mongo.feeder.MongoFeederSource
import com.ringcentral.gatling.mongo.protocol.{MongoProtocol, MongoProtocolFieldsBuilder, MongoProtocolUriBuilder}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.session.Expression
import play.api.libs.json.JsObject

import scala.concurrent.duration.{FiniteDuration, _}

trait MongoDsl extends MongoCheckSupport {

  def mongo(implicit configuration: GatlingConfiguration) = MongoProtocol

  def mongo(requestName: Expression[String])(implicit configuration: GatlingConfiguration) = new MongoDslBuilder(requestName, configuration)

  def mongoFeeder(url: String, collection: String, query: String, limit: Int = 100, batchSize: Int = 0, connectionTimeout: FiniteDuration = 5 seconds,
                  receiveTimeout: FiniteDuration = 30 seconds, postProcessor: JsObject => Map[String, Any] = MongoFeederSource.defaultPostProcessor): RecordSeqFeederBuilder[Any] =
    RecordSeqFeederBuilder(MongoFeederSource(url, collection, query, limit, batchSize, connectionTimeout, receiveTimeout, postProcessor))

  implicit def mongoProtocolUriBuilder2mongoProtocol(builder: MongoProtocolUriBuilder): MongoProtocol = builder.build()

  implicit def mongoProtocolBuilder2mongoProtocol(builder: MongoProtocolFieldsBuilder): MongoProtocol = builder.build()

  implicit def mongoCommandBuilder2ActionBuilder(commandBuilder: MongoCommandBuilder)(implicit configuration: GatlingConfiguration): ActionBuilder = {
    new MongoActionBuilder(commandBuilder.build(), configuration)
  }
}
