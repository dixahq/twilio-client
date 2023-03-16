package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class TwilioAccountSidTest
    extends SidAbstractTest[TwilioAccount.Sid, SidAbstract.SidCompanionObject[TwilioAccount.Sid]](
      TwilioAccount.Sid,
      { assertDoesNotCompile("""new TwilioAccount.Sid("invalid input")""") }
    )
