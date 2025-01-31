package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class MediaSidTest
    extends SidAbstractTest[Media.Sid, SidAbstract.SidCompanionObject[Media.Sid]](
      Media.Sid, {
        assertDoesNotCompile("""new Media.Sid("NotValidInput")""")
      }
    )
