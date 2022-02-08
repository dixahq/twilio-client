package com.dixa.twilio.client.model.phonenumber

import com.dixa.twilio.client.model.phonenumber.PhoneNumberCapabilities._
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

case class PhoneNumberCapabilities(
    voice: VoiceCapabilities,
    sms: SmsCapabilities,
    mms: MmsCapabilities,
)

object PhoneNumberCapabilities {
  sealed abstract class CallerIdPreservation(private[phonenumber] val asString: String)
      extends EnumEntry

  object CallerIdPreservation extends Enum[CallerIdPreservation] {
    override val values: immutable.IndexedSeq[CallerIdPreservation] = findValues

    case object International extends CallerIdPreservation("international")
    case object Domestic      extends CallerIdPreservation("domestic")
    case object None          extends CallerIdPreservation("none")

    private[client] def fromStringCaseInsensitive(s: String): CallerIdPreservation = findValues
      .find(_.asString.toLowerCase === s.toLowerCase)
      .getOrElse(throw new IllegalArgumentException(s"$s is not a valid CallerIdPreservation."))
  }

  sealed abstract class InboundReachability(private[phonenumber] val asString: String)
      extends EnumEntry

  object InboundReachability extends Enum[InboundReachability] {
    override val values: immutable.IndexedSeq[InboundReachability] = findValues

    case object Global   extends InboundReachability("global")
    case object Domestic extends InboundReachability("domestic")
    case object Foreign  extends InboundReachability("foreign")

    private[client] def fromStringCaseInsensitive(s: String): InboundReachability = findValues
      .find(_.asString.toLowerCase === s.toLowerCase)
      .getOrElse(throw new IllegalArgumentException(s"$s is not a valid InboundReachability."))
  }

  case class VoiceCapabilities(
      inboundConnectivity: Boolean,
      outboundConnectivity: Boolean,
      e911: Boolean,
      fax: Boolean,
      callsPerSecond: Int,
      concurrentCallsLimit: Int,
      longRecordLength: Long,
      inboundCalledDtmf: Boolean,
      inboundCallerDtmf: Boolean,
      sipTrunking: Boolean,
      inboundCallerIdPreservation: CallerIdPreservation,
      inboundReachability: InboundReachability,
  )

  case class SmsCapabilities(
      inboundConnectivity: Boolean,
      outboundConnectivity: Boolean,
      gsm7: Boolean,
      ucs2: Boolean,
      inboundSenderIdPreservation: CallerIdPreservation,
      inboundReachability: InboundReachability,
      inboundMps: Int,
  )

  case class MmsCapabilities(
      inboundConnectivity: Boolean,
      outboundConnectivity: Boolean,
      inboundReachability: InboundReachability,
      inboundMps: Int,
  )
}
