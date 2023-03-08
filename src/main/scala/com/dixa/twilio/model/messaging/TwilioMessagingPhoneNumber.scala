package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

sealed trait TwilioMessagingPhoneNumber {
  def numberSid: TwilioPhoneNumber.Sid
  def serviceSid: TwilioMessagingService.Sid
}

object TwilioMessagingPhoneNumber {

  def apply(
      activeNumberSid: TwilioPhoneNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ): TwilioMessagingPhoneNumber = DefaultImpl(activeNumberSid, serviceSid)

  private final case class DefaultImpl(
      numberSid: TwilioPhoneNumber.Sid,
      serviceSid: TwilioMessagingService.Sid
  ) extends TwilioMessagingPhoneNumber
}
