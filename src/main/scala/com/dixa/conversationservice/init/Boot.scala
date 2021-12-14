package com.dixa.conversationservice.init

import com.dixa.conversationservice.init.Config.AppConfig
import com.dixa.server.Server
import com.dixa.server.util.Stoppable
import pureconfig.generic.auto._

object Boot extends Server[AppConfig] {
  override protected def boot(config: AppConfig): Seq[Stoppable] = {
    logger.info("The application has started")
    Seq(Application(config))
  }

  run()
}
