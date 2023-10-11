package com.dixa.twilio.client.impl.general

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[general] final case class UsageTriggerListJsonRep(
    first_page_uri: String,
    usage_triggers: List[UsageTriggerJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[general] object UsageTriggerListJsonRep {

  implicit val usageTriggerListJsonRepReader: Reader[UsageTriggerListJsonRep] =
    macroR[UsageTriggerListJsonRep]
}
