package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.model.phonenumber.TwilioActiveNumber

sealed trait TwilioMessagingPhoneNumber {
  def activeNumberSid: TwilioActiveNumber.Sid
  def serviceSid: TwilioMessagingService.Sid
}

object TwilioMessagingPhoneNumber {

  def apply(
      activeNumberSid: TwilioActiveNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ): TwilioMessagingPhoneNumber = DefaultImpl(activeNumberSid, serviceSid)

  private final case class DefaultImpl(
      activeNumberSid: TwilioActiveNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ) extends TwilioMessagingPhoneNumber
}
