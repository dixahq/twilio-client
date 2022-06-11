package com.dixa.twilio.client.phonenumber

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.phonenumber.{
  TwilioActivePhoneNumber,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumberSid
}

trait TwilioClientPhoneNumber {

  /** List all incoming phonenumbers as a Source.
    *
    * A incoming phonenumber is a number that is active in twilio, and is useable for voice
    * communication. Typically also called an ActiveNumber
    *
    * The optional filter will be applied at Twilio side if set. See doc on
    * [[com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberFilter]] for details.
    */
  @deprecated("Use incomingPhoneNumberListV2 instead", "0.11.0")
  def incomingPhoneNumberList(
      connSettings: TwilioConnectionSettings,
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  ): Source[TwilioIncomingPhoneNumber, NotUsed]

  /** Lists active phone numbers for a particular Twilio subaccount as a Source.
    *
    * An active phone number is a number that is active in twilio, and is usable for voice
    * communication. Typically also called an IncomingNumber.
    *
    * The optional filter will be applied at Twilio side if set.
    */
  @deprecated("Use activePhoneNumberListV2 instead", "0.11.0")
  def activePhoneNumberList(
      connSettings: TwilioConnectionSettings,
      phoneNumber: Option[TwilioPhoneNumberSid] = None
  ): Source[TwilioActivePhoneNumber, NotUsed]

  /** Lists active phone numbers for a particular Twilio subaccount as a safe Source.
    *
    * An active phone number is a number that is active in twilio, and is usable for voice
    * communication. Typically also called an IncomingNumber.
    *
    * The optional filter will be applied at Twilio side if set.
    */
  val activePhoneNumberListV2: ActiveNumbersReadRequestExecutor

  /** List all incoming phonenumbers as a Source.
    *
    * A incoming phonenumber is a number that is active in twilio, and is useable for voice
    * communication. Typically also called an ActiveNumber
    *
    * The optional filter will be applied at Twilio side if set. See doc on
    * [[com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberFilter]] for details.
    */
  val incomingPhoneNumberListV2: IncomingNumbersReadRequestExecutor
}
