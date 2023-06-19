package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class TwimlApplicationSidTest
    extends SidAbstractTest[TwimlApplication.Sid, SidAbstract.SidCompanionObject[
      TwimlApplication.Sid
    ]](
      TwimlApplication.Sid,
      { assertDoesNotCompile("""new TwimlApplication.Sid("invalid input")""") }
    )
