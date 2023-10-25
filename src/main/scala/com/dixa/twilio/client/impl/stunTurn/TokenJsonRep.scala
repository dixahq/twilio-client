package com.dixa.twilio.client.impl.stunTurn

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{Reader, macroR}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.stunTurn.Token

import java.time.Instant

/** Json representation of a Token */
private[impl] case class TokenJsonRep(
    username: String,
    ice_servers: Seq[TokenJsonRep.IceServerJsonRep],
    date_updated: String,
    account_sid: String,
    ttl: String,
    date_created: String,
    password: String
) {

  def toModel: Token = Token(
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateUpdated = Instant.from(Formatter.dateTime.parse(date_updated)),
  )
}

private[general] object TokenJsonRep {

  private final case class IceServerJsonRep(
      url: String,
      username: Option[String],
      credential: Option[String]
                                           ) {
    def toModel: Token.IceServer = Token.IceServer(Token.IceServerUrl(url), emptyStringToNone())
  }

  private implicit val iceServerReader: Reader[IceServerJsonRep] = macroR[IceServerJsonRep]

  implicit val upickleReader: Reader[TokenJsonRep] =
    macroR[TokenJsonRep]
}
