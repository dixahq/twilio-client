package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

class ConferenceSidTest
    extends SidAbstractTest[Conference.Sid, SidAbstract.SidCompanionObject[Conference.Sid]](
      Conference.Sid, {
        assertDoesNotCompile("""new Conference.Sid("NotValidInput")""")
      }
    )
