package com.dixa.twilio.client.model.phonenumber

import com.dixa.twilio.client.model.iam.TwilioAccount

sealed trait TwilioIncomingPhoneNumber {
  def sid: TwilioPhoneNumberSid
  def accountSid: TwilioAccount.Sid
  def friendlyName: TwilioIncomingPhoneNumber.FriendlyName
  def phoneNumber: PhoneNumberE164
  def capabilities: TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary
}

object TwilioIncomingPhoneNumber {

  final case class FriendlyName(override val toString: String)

  /** Wrapper arround a string, for representing a textual contains filter.
    *
    * Some functionality working with incoming phone number support a filter, and this class
    * represent such a filter. A filter works like contains, so applying a filter, will return all
    * result where the phone number contains the filter.
    */
  final case class PhoneNumberFilter(override val toString: String)

  final case class PhoneNumberCapabilitiesSummary(
      voice: Boolean,
      sms: Boolean,
      mms: Boolean,
      fax: Boolean,
  )

  def apply(
      sid: TwilioPhoneNumberSid,
      accountSid: TwilioAccount.Sid,
      friendlyName: TwilioIncomingPhoneNumber.FriendlyName,
      phoneNumber: PhoneNumberE164,
      capabilities: PhoneNumberCapabilitiesSummary,
  ): TwilioIncomingPhoneNumber =
    DefaultImpl(sid, accountSid, friendlyName, phoneNumber, capabilities)

  private final case class DefaultImpl(
      sid: TwilioPhoneNumberSid,
      accountSid: TwilioAccount.Sid,
      friendlyName: TwilioIncomingPhoneNumber.FriendlyName,
      phoneNumber: PhoneNumberE164,
      capabilities: PhoneNumberCapabilitiesSummary,
  ) extends TwilioIncomingPhoneNumber
}
