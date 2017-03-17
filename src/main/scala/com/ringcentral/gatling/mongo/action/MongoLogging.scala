package com.ringcentral.gatling.mongo.action

import com.typesafe.scalalogging.StrictLogging

trait MongoLogging extends StrictLogging {

  def logCommand(text: => String, command: String): Unit = {
    logger.debug(text)
    logger.trace(command)
  }
}