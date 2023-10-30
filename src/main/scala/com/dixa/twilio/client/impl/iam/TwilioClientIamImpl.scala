package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.iam.{
  AccountCreateRequestExecutor,
  AccountFetchRequestExecutor,
  AccountUpdateRequestExecutor,
  AuthTokenPromoteRequestExecutor,
  AuthTokenSecondaryCreateRequestExecutor,
  AuthTokenSecondaryDeleteRequestExecutor,
  ReadAllAccountsRequestExecutor,
  TwilioClientIam
}
import com.dixa.twilio.client.impl.ApiVersion

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientIamImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientIam {

  private implicit val apiVersion: ApiVersion = ApiVersion.`2010-04-01`

  override val accountCreate: AccountCreateRequestExecutor = new AccountCreateRequestExecutorImpl()

  override val accountFetch: AccountFetchRequestExecutor = new AccountFetchRequestExecutorImpl()

  override val accountRead: ReadAllAccountsRequestExecutor =
    new ReadAllAccountsRequestExecutorImpl()

  override val accountUpdate: AccountUpdateRequestExecutor = new AccountUpdateRequestExecutorImpl()

  override val authTokenSecondaryCreate: AuthTokenSecondaryCreateRequestExecutor =
    new AuthTokenSecondaryCreateRequestExecutorImpl()

  override val authTokenSecondaryDelete: AuthTokenSecondaryDeleteRequestExecutor =
    new AuthTokenSecondaryDeleteRequestExecutorImpl()

  override val authTokenPromote: AuthTokenPromoteRequestExecutor =
    new AuthTokenPromoteRequestExecutorImpl()
}
