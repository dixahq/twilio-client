// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.{
  AccessTokenCreateRequestExecutor,
  AccountCreateRequestExecutor,
  AccountFetchRequestExecutor,
  AccountUpdateRequestExecutor,
  ApiKeyCreateRequestExecutor,
  ApiKeyDeleteRequestExecutor,
  ApiKeyReadRequestExecutor,
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

  override val accessTokenCreate: AccessTokenCreateRequestExecutor =
    new AccessTokenCreateRequestExecutorImpl()

  override val apiKeyCreate: ApiKeyCreateRequestExecutor = new ApiKeyCreateRequestExecutorImpl()

  override val apiKeyRead: ApiKeyReadRequestExecutor = new ApiKeyReadRequestExecutorImpl()

  override val apiKeyDelete: ApiKeyDeleteRequestExecutor = new ApiKeyDeleteRequestExecutorImpl()
}
