package com.dixa.twilio.client.impl.iam

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.iam.TwilioClientIam
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
