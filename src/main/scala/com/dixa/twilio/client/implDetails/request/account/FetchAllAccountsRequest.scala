package com.dixa.twilio.client.implDetails.request.account

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.implDetails.TwilioPagingFlow.NextPagePath
import com.dixa.twilio.client.implDetails.{HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.model.TwilioAccount
import io.circe.generic.auto._

private[implDetails] object FetchAllAccountsRequest {

  def apply(
      connSettings: TwilioConnectionSettings
  )(
      implicit httpExt: HttpExt,
      materializer: Materializer
  ): Source[TwilioAccount, NotUsed] = TwilioPagingFlow
    .createPagingSrc(
      connSettings,
      NextPagePath("/2010-04-01/Accounts.json?Status=active&PageSize=1000")
    )
    .map(entityToAccountList)
    .mapConcat(identity)

  // Only mapped the fields that we actually need for now, there is a lot more
  // info in these responses, that we could map once needed.
  private final case class TwilioAccountJsonRep(status: String, friendly_name: String, sid: String)
  private final case class TwilioAccountsOuterJsonRep(accounts: Vector[TwilioAccountJsonRep])
  private def entityToAccountList(entity: HttpEntityString): Seq[TwilioAccount] = {
    val decoded = entity.parseUnsafe[TwilioAccountsOuterJsonRep]()
    decoded.accounts.map { jsonRep =>
      TwilioAccount(
        TwilioAccount.Name(jsonRep.friendly_name),
        TwilioAccount.Sid(jsonRep.sid),
        TwilioAccount.Status.fromTwilioStringStatus(jsonRep.status)
      )
    }
  }
}
