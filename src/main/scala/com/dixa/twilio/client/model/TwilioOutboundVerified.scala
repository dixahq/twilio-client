package com.dixa.twilio.client.model

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class TwilioOutboundVerified(val toBoolean: Boolean) extends EnumEntry

object TwilioOutboundVerified extends Enum[TwilioOutboundVerified] {

  override def values: immutable.IndexedSeq[TwilioOutboundVerified] = findValues

  case object Verified    extends TwilioOutboundVerified(true)
  case object NotVerified extends TwilioOutboundVerified(false)

  def fromBoolean(b: Boolean): TwilioOutboundVerified = b match {
    case true  => Verified
    case false => NotVerified
  }
}
