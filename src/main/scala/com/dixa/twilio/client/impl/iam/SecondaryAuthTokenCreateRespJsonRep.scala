package com.dixa.twilio.client.impl.iam
import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}

import java.time.Instant

private[iam] final case class SecondaryAuthTokenCreateRespJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    secondary_auth_token: String
) {

  def toModel: AuthToken.AuthTokenAndMetaData[AuthToken.Secondary] = {
    val a = AuthToken.Secondary(secondary_auth_token)
    val m = AuthToken.MetaData(
      TwilioAccount.Sid(account_sid),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_created)),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_updated))
    )
    AuthToken.AuthTokenAndMetaData(a, m)
  }

  override def toString =
    s"SecondaryAuthTokenCreateRespJsonRep(account_sid=$account_sid, date_created=$date_created, date_updated=$date_updated, secondary_auth_token=***)"
}
