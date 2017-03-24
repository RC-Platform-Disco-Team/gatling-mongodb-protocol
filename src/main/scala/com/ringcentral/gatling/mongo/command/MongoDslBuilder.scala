package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import com.softwaremill.quicklens._
import reactivemongo.api.ReadPreference

case class MongoDslBuilderBase(private val commandName: Expression[String]) {
  def command(implicit configuration: GatlingConfiguration) = MongoCommandDslBuilder(commandName, configuration)

  def collection(collectionName: Expression[String])(implicit configuration: GatlingConfiguration) = MongoCollectionDslBuilder(commandName, collectionName, configuration)
}

case class MongoCommandDslBuilder(private val commandName: Expression[String], private val configuration: GatlingConfiguration) {
  def execute(command: Expression[String]) = MongoRawCommandDslBuilder(commandName, command, configuration)
}

case class MongoCollectionDslBuilder(
  private val commandName: Expression[String],
  private val collection: Expression[String],
  private val configuration: GatlingConfiguration) {

  def count() = MongoCollectionCountCommandDslBuilder(commandName, collection, configuration)
  def count(selector: Expression[String]) = MongoCollectionCountCommandDslBuilder(commandName, collection, configuration, Some(selector))

  def find(query: Expression[String]) = MongoCollectionFindCommandDslBuilder(commandName, collection, configuration, query)

  def remove(selector: Expression[String]) = MongoRemoveCommandDslBuilder(commandName, collection, selector, configuration)
  def insert(document: Expression[String]) = MongoInsertCommandDslBuilder(commandName, collection, document, configuration)
  def update(selector: Expression[String], modifier: Expression[String]) = MongoUpdateCommandDslBuilder(commandName, collection, selector, modifier, configuration)
}

trait MongoCommandBuilder {
  protected var checks: List[MongoCheck] = Nil
  protected var readPreference: ReadPreference = ReadPreference.primary

  def check(checks: MongoCheck*): MongoCommandBuilder = {
    this.checks = checks.toList
    this
  }

  def build():MongoCommand
}

case class MongoRawCommandDslBuilder(
  private val commandName: Expression[String],
  private val command: Expression[String],
  private val configuration: GatlingConfiguration) extends MongoCommandBuilder {

  def build():MongoCommand = {
    MongoRawCommand(commandName, command, checks)
  }
}

case class MongoInsertCommandDslBuilder(
  private val commandName: Expression[String],
  private val collection: Expression[String],
  private val document: Expression[String],
  private val configuration: GatlingConfiguration) extends MongoCommandBuilder {

  def build():MongoCommand = {
    MongoInsertCommand(commandName, collection,document, checks)
  }
}

case class MongoRemoveCommandDslBuilder(
  private val commandName: Expression[String],
  private val collection: Expression[String],
  private val selector: Expression[String],
  private val configuration: GatlingConfiguration) extends MongoCommandBuilder {

  def build():MongoCommand = {
    MongoRemoveCommand(commandName, collection, selector, checks)
  }
}

case class MongoUpdateCommandDslBuilder(
 private val commandName: Expression[String],
 private val collection: Expression[String],
 private val selector: Expression[String],
 private val modifier: Expression[String],
 private val configuration: GatlingConfiguration) extends MongoCommandBuilder {

  def build():MongoCommand = {
    MongoUpdateCommand(commandName, collection, selector, modifier, checks)
  }
}

case class MongoCollectionCountCommandDslBuilder(
  private val commandName: Expression[String],
  private val collection: Expression[String],
  private val configuration: GatlingConfiguration,
  private val selector: Option[Expression[String]] = None,
  private val limit: Int = 0,
  private val skip: Int = 0,
  private val hint: Option[Expression[String]] = None) extends MongoCommandBuilder {

  def hint(hint: Expression[String]): MongoCollectionCountCommandDslBuilder = this.modify(_.hint).setTo(Some(hint))

  def limit(limit: Int): MongoCollectionCountCommandDslBuilder = this.modify(_.limit).setTo(limit)

  def skip(skip: Int): MongoCollectionCountCommandDslBuilder = this.modify(_.skip).setTo(skip)

  def build():MongoCommand = {
    MongoCountCommand(commandName, collection, selector, limit, skip, hint, checks)
  }
}

case class MongoCollectionFindCommandDslBuilder(
  private val commandName: Expression[String],
  private val collection: Expression[String],
  private val configuration: GatlingConfiguration,
  private val query: Expression[String],
  private val limit: Int = 0,
  private val hint: Option[Expression[String]] = None) extends MongoCommandBuilder {

  def hint(hint: Expression[String]): MongoCollectionFindCommandDslBuilder = this.modify(_.hint).setTo(Some(hint))

  def limit(limit: Int): MongoCollectionFindCommandDslBuilder = this.modify(_.limit).setTo(limit)

  def build():MongoCommand = {
    MongoFindCommand(commandName, collection, query, limit, hint, checks)
  }
}
