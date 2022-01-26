package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.{TwilioClientAccount, TwilioConnectionSettings}
import com.dixa.twilio.client.implDetails.request.account.FetchAllAccountsRequest
import com.dixa.twilio.client.model.TwilioAccount

import scala.concurrent.ExecutionContext

private[implDetails] final class TwilioClientAccountImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientAccount {

  override def fetchAllAccounts(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status] = None
  ): Source[TwilioAccount, NotUsed] = FetchAllAccountsRequest(connSettings, status)
}
