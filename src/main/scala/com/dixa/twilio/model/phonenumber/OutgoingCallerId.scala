package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

final case class OutgoingCallerId(
    sid: OutgoingCallerId.Sid,
    friendlyName: OutgoingCallerId.FriendlyName,
    phoneNumber: PhoneNumberE164
)

object OutgoingCallerId {

  final case class Sid private[OutgoingCallerId] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(Prefix("PN"), new Sid(_))

  final case class FriendlyName(override val toString: String)

}
