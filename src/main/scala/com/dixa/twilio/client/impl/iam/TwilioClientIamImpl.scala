package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.iam.{
  AccountFetchRequestExecutor,
  ReadAllAccountsRequestExecutor,
  TwilioClientIam
}

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientIamImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientIam {

  override val accountFetch: AccountFetchRequestExecutor = new AccountFetchRequestExecutorImpl()

  override val accountRead: ReadAllAccountsRequestExecutor =
    new ReadAllAccountsRequestExecutorImpl()
}
