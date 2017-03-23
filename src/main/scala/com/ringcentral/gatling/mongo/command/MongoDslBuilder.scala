package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.action.MongoActionBuilder
import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import com.softwaremill.quicklens._
import com.typesafe.scalalogging.StrictLogging

case class MongoDslBuilderBase(commandName: Expression[String]) {
  def command(implicit configuration: GatlingConfiguration) = MongoCommandDslBuilder(commandName, configuration)

  def collection(collectionName: Expression[String])(implicit configuration: GatlingConfiguration) = MongoCollectionDslBuilder(commandName, collectionName, configuration)
}

case class MongoCommandDslBuilder(commandName: Expression[String], configuration: GatlingConfiguration) {
  def execute(command: Expression[String]) = new MongoActionBuilder(MongoRawCommand(commandName, command), configuration)
}

case class MongoCollectionDslBuilder(commandName: Expression[String], collection: Expression[String], configuration: GatlingConfiguration) {

  def count() = MongoCollectionCountCommandDslBuilder(commandName, collection, configuration)

//  def count(selector: Expression[String], limit: Int = 0, skip: Int = 0): MongoCountActionBuilder = count(selector, limit, skip)
//  def count(limit: Int, skip: Int): MongoCountActionBuilder = count(None, limit, skip)

  def find(query: Expression[String]) = new MongoActionBuilder(MongoFindCommand(commandName, collection, query), configuration)
  def remove(selector: Expression[String]) = new MongoActionBuilder(MongoRemoveCommand(commandName, collection, selector), configuration)
  def insert(document: Expression[String]) = new MongoActionBuilder(MongoInsertCommand(commandName, collection, document), configuration)
  def update(selector: Expression[String], modifier: Expression[String]) = new MongoActionBuilder(MongoUpdateCommand(commandName, collection, selector, modifier), configuration)
}

trait MongoCommandBuilder {
  def build():MongoCommand
}

case class MongoCollectionCountCommandDslBuilder(
  commandName: Expression[String],
  collection: Expression[String],
  configuration: GatlingConfiguration,
  selector: Option[Expression[String]] = None,
  limit: Int = 0,
  skip: Int = 0,
  hint: Option[Expression[String]] = None,
  checks: List[MongoCheck] = Nil) extends MongoCommandBuilder with StrictLogging {

  def hint(hint: Expression[String]): MongoCollectionCountCommandDslBuilder = this.modify(_.hint).setTo(Some(hint))

  def limit(limit: Int): MongoCollectionCountCommandDslBuilder = this.modify(_.limit).setTo(limit)

  def skip(skip: Int): MongoCollectionCountCommandDslBuilder = this.modify(_.skip).setTo(skip)

  def check(checks: MongoCheck*): MongoCollectionCountCommandDslBuilder = this.modify(_.checks).using(_ ::: checks.toList)

  def build():MongoCommand = {
    MongoCountCommand(commandName, collection, selector, limit, skip, hint, checks)
  }
}

//case class MongoCommandBuilder(command: MongoCommand, factory: MongoCommand => MongoActionBuilder) {
//  def build(): MongoActionBuilder = factory(command)
//}
