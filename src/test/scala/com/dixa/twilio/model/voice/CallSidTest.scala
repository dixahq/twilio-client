package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

class CallSidTest
    extends SidAbstractTest[Call.Sid, SidAbstract.SidCompanionObject[Call.Sid]](
      Call.Sid.asInstanceOf[SidAbstract.SidCompanionObject[Call.Sid]], {
        assertDoesNotCompile("""new com.dixa.twilio.model.voice.Call.Sid("NotValidInput")""")
      }
    )
