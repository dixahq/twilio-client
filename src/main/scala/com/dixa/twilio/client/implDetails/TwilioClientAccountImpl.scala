package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioClientAccount
import com.dixa.twilio.client.implDetails.request.account.FetchAllAccountsRequest
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[implDetails] final class TwilioClientAccountImpl()(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext,
    httpExt: HttpExt
) extends TwilioClientAccount {

  override def fetchAllAccounts(
      connSettings: TwilioConnectionSettings
  ): Source[TwilioAccount, NotUsed] = FetchAllAccountsRequest(connSettings)
}
