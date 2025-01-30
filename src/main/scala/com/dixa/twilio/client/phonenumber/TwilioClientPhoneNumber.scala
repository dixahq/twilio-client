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

  /** Delete an incoming phone number from twilio account.
    *
    * An IncomingPhoneNumber instance resource represents a Twilio phone number provisioned from
    * Twilio, ported or hosted to Twilio.
    *
    * @see
    *   https://www.twilio.com/docs/phone-numbers/api/incomingphonenumber-resource#delete-an-incomingphonenumber-resource
    */
  def incomingPhoneNumberDelete: IncomingPhoneNumberDeleteRequestExecutor

  /** Lists active phone numbers for a particular Twilio subaccount as a Source.
    *
    * An active phone number is a number that is active in twilio, and is usable for voice
    * communication. Typically also called an IncomingNumber.
    *
    * The optional filter will be applied at Twilio side if set.
    */
  def activePhoneNumberList: ActiveNumbersReadRequestExecutor

  /** Lists outgoing caller ID's for a particular Twilio subaccount as a Source.
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    *
    * The optional filter will be applied at Twilio side if set.
    */
  def outgoingCallerIdList: OutgoingCallerIdReadRequestExecutor

  /** Deletes outgoing caller ID for a particular Twilio subaccount
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    */
  def outgoingCallerIdDelete: OutgoingCallerIdDeleteRequestExecutor

  /** Creates outgoing caller ID for a particular Twilio sub account
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    *
    * @see
    *   https://www.twilio.com/docs/voice/api/outgoing-caller-ids#http-post
    */
  def outgoingCallerIdCreate: OutgoingCallerIdCreateRequestExecutor
}
