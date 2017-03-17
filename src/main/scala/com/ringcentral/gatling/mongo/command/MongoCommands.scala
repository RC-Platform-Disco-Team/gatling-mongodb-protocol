package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.session.Expression

abstract class MongoCommand(var checks: List[MongoCheck] = Nil) {

}

case class MongoRawCommand(commandName: Expression[String], command: Expression[String]) extends MongoCommand
case class MongoCountCommand(commandName: Expression[String], collection: Expression[String]) extends MongoCommand
case class MongoInsertCommand(commandName: Expression[String], collection: Expression[String], document: Expression[String]) extends MongoCommand
case class MongoRemoveCommand(commandName: Expression[String], collection: Expression[String], selector: Expression[String]) extends MongoCommand
case class MongoFindCommand(commandName: Expression[String], collection: Expression[String], query: Expression[String], limit: Int = 50) extends MongoCommand
case class MongoUpdateCommand(commandName: Expression[String], collection: Expression[String], selector: Expression[String], modifier: Expression[String]) extends MongoCommand