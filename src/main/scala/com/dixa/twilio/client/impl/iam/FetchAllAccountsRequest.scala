package com.dixa.twilio.client.impl.iam

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.model.iam.TwilioAccount
import io.circe.generic.auto._

private[impl] object FetchAllAccountsRequest {

  def apply(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status]
  )(
      implicit httpExt: HttpExt,
      materializer: Materializer
  ): Source[TwilioAccount, NotUsed] = {
    val statusParam = status.map(s => s"&Status=${s.apiName}").getOrElse("")
    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(
          ApiSubDomain.Api,
          HttpMethods.GET,
          s"/2010-04-01/Accounts.json?PageSize=1000$statusParam"
        )
      )
      .map(entityToAccountList)
      .mapConcat(identity)
  }

  private final case class TwilioAccountsOuterJsonRep(accounts: Vector[TwilioAccountJsonRep])
  private def entityToAccountList(entity: HttpEntityString): Seq[TwilioAccount] = {
    val decoded = entity.parseUnsafe[TwilioAccountsOuterJsonRep]()
    decoded.accounts.map { jsonRep =>
      TwilioAccount(
        TwilioAccount.Name(jsonRep.friendly_name),
        TwilioAccount.Sid(jsonRep.sid),
        TwilioAccount.Status.fromApiName(jsonRep.status),
        TwilioAccount.Sid(jsonRep.owner_account_sid),
        TwilioAccount.AuthToken(jsonRep.auth_token),
        TwilioAccount.Type.fromTwilioApiName(jsonRep.`type`),
      )
    }
  }
}
