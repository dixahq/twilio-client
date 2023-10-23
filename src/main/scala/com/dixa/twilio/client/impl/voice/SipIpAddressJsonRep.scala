package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

import java.time.Instant

final case class SipIpAddressJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    friendly_name: String,
    ip_access_control_list_sid: String,
    ip_address: String,
    cidr_prefix_length: Option[String],
    sid: String
) {

  def toModelUnsafe: SipIpAddress = SipIpAddress(
    SipIpAddress.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    SipIpAddress.FriendlyName.unsafe(friendly_name),
    SipIpAddress.IpAddress.unsafe(ip_address),
    emptyStringToNone(cidr_prefix_length).map(SipIpAddress.CidrPrefixLength.fromTwilioStringUnsafe),
    IpAccessControlList.Sid.unsafe(ip_access_control_list_sid),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[voice] object SipIpAddressJsonRep {

  implicit val upickleReader: Reader[SipIpAddressJsonRep] =
    macroR[SipIpAddressJsonRep]
}
