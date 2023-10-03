package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.{SidAbstract, TwilioStringValue}

import java.time.{Duration, Instant}

final case class Queue(
    sid: Queue.Sid,
    friendlyName: Queue.FriendlyName,
    accountSid: TwilioAccount.Sid,
    currentSize: Queue.CurrentSize,
    maxSize: Queue.MaxSize,
    averageWaitTime: Duration,
    dateCreated: Instant,
    dateUpdated: Instant
)

object Queue {

  final case class Sid private[Queue] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("QU")), new Sid(_))

  final case class FriendlyName(override val toString: String) extends TwilioStringValue

  final case class CurrentSize(asInt: Int) extends TwilioStringValue {
    override def twilioString: String = asInt.toString
  }

  /** Represent the max size of the Queue.
    *
    * This value has to be between 1 and 5000. This class treat is as a Int, so that this library
    * would continue to be useable even if Twilio change that limit, but there is a Enumeration in
    * the companion object, representing all allowed values, that can be used if you want to force
    * valid values via the type system.
    */
  final case class MaxSize(asInt: Int) extends TwilioStringValue {
    override def twilioString: String = asInt.toString
  }

}
