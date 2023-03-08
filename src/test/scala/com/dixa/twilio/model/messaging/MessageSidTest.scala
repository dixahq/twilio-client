package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class MessageSidTest
    extends SidAbstractTest[Message.Sid, SidAbstract.SidCompanionObject[Message.Sid]](
      Message.Sid,
      { assertDoesNotCompile("""new Message.Sid("NotValidInput")""") }
    )
