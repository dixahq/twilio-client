package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.IpAccessControlList

import java.time.Instant

final case class IpAccessControlListJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    friendly_name: Option[String],
    sid: String,
) {

  def toModelUnsafe: IpAccessControlList = IpAccessControlList(
    TwilioAccount.Sid.unsafe(account_sid),
    IpAccessControlList.Sid.unsafe(sid),
    emptyStringToNone(friendly_name).map(IpAccessControlList.FriendlyName.unsafe),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[voice] object IpAccessControlListJsonRep {

  implicit val upickleReader: Reader[IpAccessControlListJsonRep] =
    macroR[IpAccessControlListJsonRep]
}
