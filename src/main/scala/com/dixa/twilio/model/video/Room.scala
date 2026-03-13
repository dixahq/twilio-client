package com.dixa.twilio.model.video

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{SidAbstract, TwilioStringValue}

/** Represent a Room SID or Unique Name.
  *
  * When used in a Video Grant, it can be either a Room SID (starts with RM) or a Unique Name.
  *
  * @see
  *   https://www.twilio.com/docs/video/api/rooms-resource#room-sid
  */
sealed trait Room extends TwilioStringValue

object Room {

  /** Represent a Twilio Room SID
    *
    * It is a 34 character string that starts with RM.
    */
  final case class Sid private[Room] (override val toString: String) extends SidAbstract with Room

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("RM")), new Sid(_))

  /** Represent a Twilio Room Unique Name
    */
  final case class UniqueName private[Room] (override val toString: String) extends Room

  /** Construct a Room from a string.
    *
    * If the string is a valid Room SID, it will be a Sid instance. Otherwise, it will be a
    * UniqueName instance.
    */
  def apply(value: String): Room = {
    Sid.safe(value).getOrElse(UniqueName(value))
  }
}
