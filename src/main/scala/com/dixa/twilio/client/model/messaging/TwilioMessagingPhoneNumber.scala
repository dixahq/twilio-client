package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.model.phonenumber.ActiveNumber

sealed trait TwilioMessagingPhoneNumber {
  def activeNumberSid: ActiveNumber.Sid
  def serviceSid: TwilioMessagingService.Sid
}

object TwilioMessagingPhoneNumber {

  def apply(
      activeNumberSid: ActiveNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ): TwilioMessagingPhoneNumber = DefaultImpl(activeNumberSid, serviceSid)

  private final case class DefaultImpl(
      activeNumberSid: ActiveNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ) extends TwilioMessagingPhoneNumber
}
