package com.dixa.twilio.client.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.{TwilioClientIam, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.request.iam.FetchAllAccountsRequest
import com.dixa.twilio.client.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientIamImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientIam {

  override def fetchAllAccounts(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status] = None
  ): Source[TwilioAccount, NotUsed] = FetchAllAccountsRequest(connSettings, status)
}
