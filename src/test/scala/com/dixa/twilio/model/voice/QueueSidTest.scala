package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

class QueueSidTest
    extends SidAbstractTest[Queue.Sid, SidAbstract.SidCompanionObject[Queue.Sid]](
      Queue.Sid,
      { assertDoesNotCompile("""new Queue.Sid("NotValidInput")""") }
    )
