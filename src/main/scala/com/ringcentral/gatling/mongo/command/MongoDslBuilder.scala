package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.action.MongoCommandActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

case class MongoDslBuilderBase(commandName: Expression[String]) {
  def command(implicit configuration: GatlingConfiguration) = MongoCommandDslBuilder(commandName, configuration)

  def collection(collectionName: Expression[String])(implicit configuration: GatlingConfiguration) = MongoCollectionDslBuilder(commandName, collectionName, configuration)
}

case class MongoCommandDslBuilder(commandName: Expression[String], configuration: GatlingConfiguration) {
  def execute(command: Expression[String]) = MongoCommandActionBuilder(MongoRawCommand(commandName, command), configuration)
}

case class MongoCollectionDslBuilder(commandName: Expression[String], collection: Expression[String], configuration: GatlingConfiguration) {
  def count = MongoCommandActionBuilder(MongoCountCommand(commandName, collection), configuration)
  def find(query: Expression[String]) = MongoCommandActionBuilder(MongoFindCommand(commandName, collection, query), configuration)
  def remove(selector: Expression[String]) = MongoCommandActionBuilder(MongoRemoveCommand(commandName, collection, selector), configuration)
  def insert(document: Expression[String]) = MongoCommandActionBuilder(MongoInsertCommand(commandName, collection, document), configuration)
  def update(selector: Expression[String], modifier: Expression[String]) = MongoCommandActionBuilder(MongoUpdateCommand(commandName, collection, selector, modifier), configuration)
}
