package com.dixa.twilio.model.phonenumber

final case class OutgoingCallerId(
    sid: OutgoingCallerId.Sid,
    friendlyName: OutgoingCallerId.FriendlyName,
    phoneNumber: PhoneNumberE164
)

object OutgoingCallerId {

  final case class Sid(override val toString: String)

  final case class FriendlyName(override val toString: String)

}
