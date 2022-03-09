package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.phonenumber.PhoneNumberCapabilities._
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

final case class PhoneNumberCapabilities(
    voice: VoiceCapabilities,
    sms: SmsCapabilities,
    mms: MmsCapabilities,
)

object PhoneNumberCapabilities {
  sealed abstract class CallerIdPreservation(val apiName: String) extends EnumEntry

  object CallerIdPreservation extends Enum[CallerIdPreservation] {
    override val values: immutable.IndexedSeq[CallerIdPreservation] = findValues

    case object International extends CallerIdPreservation("international")
    case object Domestic      extends CallerIdPreservation("domestic")
    case object None          extends CallerIdPreservation("none")

    def fromApiNameCaseInsensitive(s: String): CallerIdPreservation = values
      .find(_.apiName.toLowerCase === s.toLowerCase)
      .getOrElse(
        throw new IllegalArgumentException(
          s"$s is not a valid CallerIdPreservation. Valid values are: $values"
        )
      )
  }

  sealed abstract class InboundReachability(val apiName: String) extends EnumEntry

  object InboundReachability extends Enum[InboundReachability] {
    override val values: immutable.IndexedSeq[InboundReachability] = findValues

    case object Global   extends InboundReachability("global")
    case object Domestic extends InboundReachability("domestic")
    case object Foreign  extends InboundReachability("foreign")

    def fromApiNameCaseInsensitive(s: String): InboundReachability = values
      .find(_.apiName.toLowerCase === s.toLowerCase)
      .getOrElse(
        throw new IllegalArgumentException(
          s"$s is not a valid InboundReachability. Valid values are: $values"
        )
      )
  }

  final case class VoiceCapabilities(
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

  final case class SmsCapabilities(
      inboundConnectivity: Boolean,
      outboundConnectivity: Boolean,
      gsm7: Boolean,
      ucs2: Boolean,
      inboundSenderIdPreservation: CallerIdPreservation,
      inboundReachability: InboundReachability,
      inboundMps: Int,
  )

  final case class MmsCapabilities(
      inboundConnectivity: Boolean,
      outboundConnectivity: Boolean,
      inboundReachability: InboundReachability,
      inboundMps: Int,
  )
}
