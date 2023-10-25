package com.dixa.twilio.model.stunTurn

import com.dixa.twilio.model.PositiveInteger
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** Represent a Network Traversal Service Token.
  *
  * @see
  *   https://www.twilio.com/docs/stun-turn/api
  */
final case class Token(
    username: Token.Username,
    password: Token.Password,
    ttl: PositiveInteger,
    accountSid: TwilioAccount.Sid,
    iceServers: Seq[Token.IceServer],
    dateCreated: Instant,
    dateUpdated: Instant
)

object Token {

  final case class Username(override val toString: String)

  final case class Password(override val toString: String)

  final case class IceServerUrl(override val toString: String)

  final case class IceServerUsername(override val toString: String)
  final case class IceServerCredential(override val toString: String)

  final case class IceServer(
      urls: IceServerUrl,
      username: Option[IceServerUsername],
      credential: Option[IceServerCredential]
  )

}
