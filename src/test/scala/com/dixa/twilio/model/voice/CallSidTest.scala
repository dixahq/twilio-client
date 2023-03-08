package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

class CallSidTest
    extends SidAbstractTest[Call.Sid, SidAbstract.SidCompanionObject[Call.Sid]](
      Call.Sid,
      { assertDoesNotCompile("""new Call.Sid("NotValidInput")""") }
    )
