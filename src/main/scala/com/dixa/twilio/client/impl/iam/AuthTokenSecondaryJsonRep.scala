package com.dixa.twilio.client.impl.iam
import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}

import java.time.Instant

/** Representation of the Json twilio uses for auth tokens */
private[iam] final case class AuthTokenSecondaryJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    secondary_auth_token: String
) {

  def toModel: AuthToken.AuthTokenAndMetaData[AuthToken.Secondary] = {
    val token = AuthToken.Secondary(secondary_auth_token)
    val metadata = AuthToken.MetaData(
      TwilioAccount.Sid(account_sid),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_created)),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_updated))
    )
    AuthToken.AuthTokenAndMetaData(token, metadata)
  }

  override def toString =
    s"AuthTokenSecondaryJsonRep(account_sid=$account_sid, date_created=$date_created, date_updated=$date_updated, secondary_auth_token=***)"
}
