package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.{ConstrainedString, SidAbstract, TwilioStringValue}
import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** Represents a outgoing caller id
  *
  * An OutgoingCallerId instance resource represents a single verified number that may be used as a
  * caller ID when making outgoing calls
  *
  * @see
  *   https://www.twilio.com/docs/voice/api/outgoing-caller-ids#outgoingcallerid-instance-resource
  */
final case class OutgoingCallerId(
    sid: OutgoingCallerId.Sid,
    accountSid: TwilioAccount.Sid,
    friendlyName: Option[OutgoingCallerId.FriendlyName],
    phoneNumber: PhoneNumberE164,
    dateCreated: Instant,
    dateUpdated: Instant,
)

object OutgoingCallerId {

  final case class Sid private[OutgoingCallerId] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("PN")), new Sid(_))

  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue

  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName](64) {
    override def constructInstance(wrapped: String) = new FriendlyName(wrapped)
  }
}
