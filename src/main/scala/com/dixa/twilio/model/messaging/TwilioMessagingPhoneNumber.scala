package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.phonenumber.TwilioPhoneNumberSid

sealed trait TwilioMessagingPhoneNumber {
  def numberSid: TwilioPhoneNumberSid
  def serviceSid: TwilioMessagingService.Sid
}

object TwilioMessagingPhoneNumber {

  def apply(
      activeNumberSid: TwilioPhoneNumberSid,
      serviceSid: TwilioMessagingService.Sid
  ): TwilioMessagingPhoneNumber = DefaultImpl(activeNumberSid, serviceSid)

  private final case class DefaultImpl(
      numberSid: TwilioPhoneNumberSid,
      serviceSid: TwilioMessagingService.Sid
  ) extends TwilioMessagingPhoneNumber
}
