package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.session.Expression

abstract class MongoCommand(checks: List[MongoCheck])
case class MongoRawCommand(commandName: Expression[String], command: Expression[String], checks: List[MongoCheck] = Nil) extends MongoCommand(checks)
case class MongoCountCommand(commandName: Expression[String], collection: Expression[String], selector: Option[Expression[String]] = None, limit: Int = 50, skip: Int = 50, hint: Option[Expression[String]] = None, checks: List[MongoCheck] = Nil) extends MongoCommand(checks)
case class MongoInsertCommand(commandName: Expression[String], collection: Expression[String], document: Expression[String], checks: List[MongoCheck] = Nil) extends MongoCommand(checks)
case class MongoRemoveCommand(commandName: Expression[String], collection: Expression[String], selector: Expression[String], checks: List[MongoCheck] = Nil) extends MongoCommand(checks)
case class MongoFindCommand(commandName: Expression[String], collection: Expression[String], query: Expression[String], limit: Int = 50, hint: Option[Expression[String]] = None, checks: List[MongoCheck] = Nil) extends MongoCommand(checks)
case class MongoUpdateCommand(commandName: Expression[String], collection: Expression[String], selector: Expression[String], modifier: Expression[String], checks: List[MongoCheck] = Nil) extends MongoCommand(checks)