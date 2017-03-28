package com.ringcentral.gatling.mongo

import com.ringcentral.gatling.mongo.action.MongoActionBuilder
import com.ringcentral.gatling.mongo.check.MongoCheckSupport
import com.ringcentral.gatling.mongo.command.{MongoCommandBuilder, MongoDslBuilderBase}
import com.ringcentral.gatling.mongo.feeder.MongoFeederSource
import com.ringcentral.gatling.mongo.protocol.{MongoProtocol, MongoProtocolBuilder, MongoProtocolBuilderBase, MongoProtocolUriBuilder}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.session.Expression
import play.api.libs.json.JsObject

import scala.concurrent.duration.{FiniteDuration, _}

trait MongoDsl extends MongoCheckSupport {

  def mongo(implicit configuration: GatlingConfiguration) = MongoProtocolBuilderBase
  def mongo(requestName: Expression[String]) = MongoDslBuilderBase(requestName)

  def mongoFeeder(url: String, collection: String, query: String, limit: Int = 100, batchSize: Int = 0, connectionTimeout: FiniteDuration = 5 seconds, receiveTimeout: FiniteDuration = 30 seconds, postProcessor: JsObject => Map[String, Any] = MongoFeederSource.defaultPostProcessor): RecordSeqFeederBuilder[Any] =
    RecordSeqFeederBuilder(MongoFeederSource(url, collection, query, limit, batchSize, connectionTimeout, receiveTimeout, postProcessor))

  implicit def mongoProtocolUriBuilder2mongoProtocol(builder: MongoProtocolUriBuilder): MongoProtocol = builder.build()
  implicit def mongoProtocolBuilder2mongoProtocol(builder: MongoProtocolBuilder): MongoProtocol = builder.build()


  implicit def mongoCommandBuilder2ActionBuilder(commandBuilder: MongoCommandBuilder)(implicit configuration: GatlingConfiguration): ActionBuilder = {
    new MongoActionBuilder(commandBuilder.build(), configuration)
  }
}
