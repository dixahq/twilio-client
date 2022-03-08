package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid

sealed trait TwilioMessagingPhoneNumber {
  def numberSid: TwilioPhoneNumberSid
  def serviceSid: ServiceSid
}

object TwilioMessagingPhoneNumber {

  def apply(
      activeNumberSid: TwilioPhoneNumberSid,
      serviceSid: ServiceSid
  ): TwilioMessagingPhoneNumber = DefaultImpl(activeNumberSid, serviceSid)

  private final case class DefaultImpl(
      numberSid: TwilioPhoneNumberSid,
      serviceSid: ServiceSid
  ) extends TwilioMessagingPhoneNumber
}
