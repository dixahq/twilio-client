package com.dixa.twilio.client.phonenumber

trait TwilioClientPhoneNumber {

  /** List all incoming phonenumbers as a Source.
    *
    * A incoming phonenumber is a number that is active in twilio, and is useable for voice
    * communication. Typically also called an ActiveNumber
    *
    * The optional filter will be applied at Twilio side if set. See doc on
    * [[com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberFilter]] for details.
    */
  def incomingPhoneNumberList: IncomingNumbersReadRequestExecutor

  /** Lists active phone numbers for a particular Twilio subaccount as a Source.
    *
    * An active phone number is a number that is active in twilio, and is usable for voice
    * communication. Typically also called an IncomingNumber.
    *
    * The optional filter will be applied at Twilio side if set.
    */
  def activePhoneNumberList: ActiveNumbersReadRequestExecutor
}
