package com.dixa.conversationservice.init

import com.dixa.dynamoutil.DynamoConfig

//TODO this object should mimic application.conf
object Config {
  case class ConversationService(bindHost: String, bindPort: Int)
  //TODO pass all dependencies configurations
  case class AppConfig(service: ConversationService, dynamodb: DynamoConfig)
}
