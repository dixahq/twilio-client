package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract, TwilioStringValue}
import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant
import scala.collection.immutable

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

  final case class FriendlyName(override val toString: String) extends TwilioStringValue

  final case class Extension(value: BigDecimal) extends TwilioStringValue {
    def digits = {
      value.toString().map(_.asDigit)
    }

    override val toString: String = digits.toString()
  }

  sealed abstract class CallDelay(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry
  object CallDelay extends EnumWithTwilioString[CallDelay] {
    override val values: immutable.IndexedSeq[CallDelay] = findValues
    case object Seconds1  extends CallDelay("1")
    case object Seconds2  extends CallDelay("2")
    case object Seconds3  extends CallDelay("3")
    case object Seconds4  extends CallDelay("4")
    case object Seconds5  extends CallDelay("5")
    case object Seconds6  extends CallDelay("6")
    case object Seconds7  extends CallDelay("7")
    case object Seconds8  extends CallDelay("8")
    case object Seconds9  extends CallDelay("9")
    case object Seconds10 extends CallDelay("10")
    case object Seconds11 extends CallDelay("11")
    case object Seconds12 extends CallDelay("12")
    case object Seconds13 extends CallDelay("13")
    case object Seconds14 extends CallDelay("14")
    case object Seconds15 extends CallDelay("15")
    case object Seconds16 extends CallDelay("16")
    case object Seconds17 extends CallDelay("17")
    case object Seconds18 extends CallDelay("18")
    case object Seconds19 extends CallDelay("19")
    case object Seconds20 extends CallDelay("20")
    case object Seconds21 extends CallDelay("21")
    case object Seconds22 extends CallDelay("22")
    case object Seconds23 extends CallDelay("23")
    case object Seconds24 extends CallDelay("24")
    case object Seconds25 extends CallDelay("25")
    case object Seconds26 extends CallDelay("26")
    case object Seconds27 extends CallDelay("27")
    case object Seconds28 extends CallDelay("28")
    case object Seconds29 extends CallDelay("29")
    case object Seconds30 extends CallDelay("30")
    case object Seconds31 extends CallDelay("31")
    case object Seconds32 extends CallDelay("32")
    case object Seconds33 extends CallDelay("33")
    case object Seconds34 extends CallDelay("34")
    case object Seconds35 extends CallDelay("35")
    case object Seconds36 extends CallDelay("36")
    case object Seconds37 extends CallDelay("37")
    case object Seconds38 extends CallDelay("38")
    case object Seconds39 extends CallDelay("39")
    case object Seconds40 extends CallDelay("40")
    case object Seconds41 extends CallDelay("41")
    case object Seconds42 extends CallDelay("42")
    case object Seconds43 extends CallDelay("43")
    case object Seconds44 extends CallDelay("44")
    case object Seconds45 extends CallDelay("45")
    case object Seconds46 extends CallDelay("46")
    case object Seconds47 extends CallDelay("47")
    case object Seconds48 extends CallDelay("48")
    case object Seconds49 extends CallDelay("49")
    case object Seconds50 extends CallDelay("50")
    case object Seconds51 extends CallDelay("51")
    case object Seconds52 extends CallDelay("52")
    case object Seconds53 extends CallDelay("53")
    case object Seconds54 extends CallDelay("54")
    case object Seconds55 extends CallDelay("55")
    case object Seconds56 extends CallDelay("56")
    case object Seconds57 extends CallDelay("57")
    case object Seconds58 extends CallDelay("58")
    case object Seconds59 extends CallDelay("59")
    case object Seconds60 extends CallDelay("60")

  }
}
