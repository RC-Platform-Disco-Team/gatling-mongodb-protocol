package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import reactivemongo.api.ReadPreference

//TODO remove Expression
class MongoDslBuilder(private val commandName: Expression[String], implicit private val configuration: GatlingConfiguration) {

  def execute(command: Expression[String]) = MongoRawCommandBuilder(commandName, command)

  def collection(collectionName: Expression[String]) = new MongoCollectionDslBuilder(commandName, collectionName)
}

class MongoCollectionDslBuilder(private val commandName: Expression[String],
                                private val collection: Expression[String]) {

  def count() = MongoCountCommandBuilder(commandName, collection)

  def count(selector: Expression[String]) = MongoCountCommandBuilder(commandName, collection, Some(selector))

  def find(query: Expression[String]) = MongoFindCommandBuilder(commandName, collection, query)

  def remove(selector: Expression[String]) = MongoRemoveCommandBuilder(commandName, collection, selector)

  def insert(document: Expression[String]) = MongoInsertCommandBuilder(commandName, collection, document)

  def update(selector: Expression[String], modifier: Expression[String]) = MongoUpdateCommandBuilder(commandName, collection, selector, modifier)
}

trait MongoCommandBuilder {

  protected var readPreference: ReadPreference = ReadPreference.primary
  protected var checks: List[MongoCheck] = Nil

  def check(checks: MongoCheck*): MongoCommandBuilder = {
    this.checks = checks.toList
    this
  }

  def readPreference(readPreference: ReadPreference): MongoCommandBuilder = {
    this.readPreference = readPreference
    this
  }

  def build(): MongoCommand
}

case class MongoRawCommandBuilder(private val commandName: Expression[String],
                                  private val command: Expression[String]) extends MongoCommandBuilder {

  def build(): MongoCommand = MongoRawCommand(commandName, command, checks)
}

case class MongoInsertCommandBuilder(private val commandName: Expression[String],
                                     private val collection: Expression[String],
                                     private val document: Expression[String]) extends MongoCommandBuilder {

  def build(): MongoCommand = MongoInsertCommand(commandName, collection, document, checks)
}

case class MongoRemoveCommandBuilder(private val commandName: Expression[String],
                                     private val collection: Expression[String],
                                     private val selector: Expression[String]) extends MongoCommandBuilder {

  def build(): MongoCommand = MongoRemoveCommand(commandName, collection, selector, checks)
}

case class MongoUpdateCommandBuilder(private val commandName: Expression[String],
                                     private val collection: Expression[String],
                                     private val selector: Expression[String],
                                     private val modifier: Expression[String]) extends MongoCommandBuilder {

  def build(): MongoCommand = MongoUpdateCommand(commandName, collection, selector, modifier, checks)
}

case class MongoCountCommandBuilder(private val commandName: Expression[String],
                                    private val collection: Expression[String],
                                    private val selector: Option[Expression[String]] = None,
                                    private val limit: Int = 0,
                                    private val skip: Int = 0,
                                    private val hint: Option[Expression[String]] = None) extends MongoCommandBuilder {

  def hint(hint: Expression[String]): MongoCountCommandBuilder = copy(hint = Some(hint))

  def limit(limit: Int): MongoCountCommandBuilder = copy(limit = limit)

  def skip(skip: Int): MongoCountCommandBuilder = copy(skip = skip)

  def build(): MongoCommand = MongoCountCommand(commandName, collection, selector, limit, skip, checks, hint)
}

case class MongoFindCommandBuilder(private val commandName: Expression[String],
                                   private val collection: Expression[String],
                                   private val query: Expression[String],
                                   private val limit: Int = 0,
                                   private val skip: Int = 0,
                                   private val sort: Option[Expression[String]] = None,
                                   private val hint: Option[Expression[String]] = None) extends MongoCommandBuilder {

  def sort(sort: Expression[String]): MongoFindCommandBuilder = copy(sort = Some(sort))

  def hint(hint: Expression[String]): MongoFindCommandBuilder = copy(hint = Some(hint))

  def limit(limit: Int): MongoFindCommandBuilder = copy(limit = limit)

  def skip(skip: Int): MongoFindCommandBuilder = copy(skip = skip)

  def build(): MongoCommand = MongoFindCommand(commandName, collection, query, limit, sort, checks, hint)
}
