package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.phonenumber.TwilioIncomingPhoneNumber

trait TwilioClientPhoneNumber {

  /** List all incoming phonenumbers as a Source.
    *
    * A incoming phonenumber is a number that is active in twilio, and is useable for voice
    * communication. Typically also called an ActiveNumber
    *
    * The optional filter will be applied at Twilio side if set. See doc on
    * [[com.dixa.twilio.client.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberFilter]] for
    * details.
    */
  def incomingPhoneNumberList(
      connSettings: TwilioConnectionSettings,
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  ): Source[TwilioIncomingPhoneNumber, NotUsed]
}
