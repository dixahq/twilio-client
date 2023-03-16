package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class OutgoingCallerIdSidTest
    extends SidAbstractTest[OutgoingCallerId.Sid, SidAbstract.SidCompanionObject[
      OutgoingCallerId.Sid
    ]](
      OutgoingCallerId.Sid,
      { assertDoesNotCompile("""new OutgoingCallerId.Sid("NotValidInput")""") }
    )
