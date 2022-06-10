package com.dixa.twilio.client.impl.iam

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.iam.{
  AccountFetchRequestExecutor,
  ReadAllAccountsRequestExecutor,
  TwilioClientIam
}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientIamImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientIam {

  override def accountFetch: AccountFetchRequestExecutor = new AccountFetchRequestExecutorImpl()

  @deprecated("Use accountReadV2 instead", "0.11.0")
  override def accountRead(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status] = None
  ): Source[TwilioAccount, NotUsed] = FetchAllAccountsRequest.apply(connSettings, status)

  override def accountReadV2: ReadAllAccountsRequestExecutor =
    new ReadAllAccountsRequestExecutorImpl()
}
