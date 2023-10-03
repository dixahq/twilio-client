package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.{SidAbstract, SidAbstractTest}
import org.scalatest.Assertions.assertDoesNotCompile

final class TwilioMessagingServiceSidTest
    extends SidAbstractTest[TwilioMessagingService.Sid, SidAbstract.SidCompanionObject[
      TwilioMessagingService.Sid
    ]](
      TwilioMessagingService.Sid,
      { assertDoesNotCompile("""new TwilioMessagingService.Sid("NotValidInput")""") }
    )
