package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.phonenumber.PhoneNumberCapabilities._

import scala.collection.immutable

final case class PhoneNumberCapabilities(
    voice: VoiceCapabilities,
    sms: SmsCapabilities,
    mms: MmsCapabilities,
)

object PhoneNumberCapabilities {
  sealed abstract class CallerIdPreservation(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object CallerIdPreservation extends EnumWithTwilioString[CallerIdPreservation] {
    override val values: immutable.IndexedSeq[CallerIdPreservation] = findValues

    case object International extends CallerIdPreservation("international")
    case object Domestic      extends CallerIdPreservation("domestic")
    case object None          extends CallerIdPreservation("none")
  }

  sealed abstract class InboundReachability(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object InboundReachability extends EnumWithTwilioString[InboundReachability] {
    override val values: immutable.IndexedSeq[InboundReachability] = findValues

    case object Global   extends InboundReachability("global")
    case object Domestic extends InboundReachability("domestic")
    case object Foreign  extends InboundReachability("foreign")
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
