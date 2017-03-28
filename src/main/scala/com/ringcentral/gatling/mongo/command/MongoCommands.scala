package com.ringcentral.gatling.mongo.command

import com.ringcentral.gatling.mongo.check.MongoCheck
import io.gatling.core.session.Expression

trait MongoCommand {
  val commandName: Expression[String]
  val checks: List[MongoCheck]
}

trait MongoCommandOnCollection extends MongoCommand {
  val collection: Expression[String]
}

trait Hint {
  val hint: Option[Expression[String]]
}

case class MongoRawCommand(commandName: Expression[String],
                           command: Expression[String],
                           checks: List[MongoCheck] = List.empty) extends MongoCommand

case class MongoCountCommand(commandName: Expression[String],
                             collection: Expression[String],
                             selector: Option[Expression[String]] = None,
                             limit: Int = 50,
                             skip: Int = 0,
                             checks: List[MongoCheck] = List.empty,
                             hint: Option[Expression[String]] = None) extends MongoCommandOnCollection with Hint

case class MongoFindCommand(commandName: Expression[String],
                            collection: Expression[String],
                            query: Expression[String],
                            limit: Int = 50,
                            sort: Option[Expression[String]] = None,
                            checks: List[MongoCheck] = List.empty,
                            hint: Option[Expression[String]] = None) extends MongoCommandOnCollection with Hint

case class MongoInsertCommand(commandName: Expression[String],
                              collection: Expression[String],
                              document: Expression[String],
                              checks: List[MongoCheck] = List.empty) extends MongoCommandOnCollection

case class MongoRemoveCommand(commandName: Expression[String],
                              collection: Expression[String],
                              selector: Expression[String],
                              checks: List[MongoCheck] = List.empty) extends MongoCommandOnCollection

case class MongoUpdateCommand(commandName: Expression[String],
                              collection: Expression[String],
                              selector: Expression[String],
                              modifier: Expression[String],
                              checks: List[MongoCheck] = List.empty) extends MongoCommandOnCollection