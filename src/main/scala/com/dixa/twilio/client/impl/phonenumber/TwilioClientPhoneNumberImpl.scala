// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.phonenumber

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.ApiVersion
import com.dixa.twilio.client.phonenumber.{
  ActiveNumbersReadRequestExecutor,
  IncomingNumbersReadRequestExecutor,
  IncomingPhoneNumberDeleteRequestExecutor,
  OutgoingCallerIdCreateRequestExecutor,
  OutgoingCallerIdDeleteRequestExecutor,
  OutgoingCallerIdReadRequestExecutor,
  TwilioClientPhoneNumber
}

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientPhoneNumberImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientPhoneNumber {

  private implicit val apiVersion: ApiVersion = ApiVersion.`2010-04-01`

  override val incomingPhoneNumberList: IncomingNumbersReadRequestExecutor =
    new IncomingNumbersReadRequestExecutorImpl()

  override val incomingPhoneNumberDelete: IncomingPhoneNumberDeleteRequestExecutor =
    new IncomingPhoneNumberDeleteRequestExecutorImpl()

  override val activePhoneNumberList: ActiveNumbersReadRequestExecutor =
    new ActiveNumbersReadRequestExecutorImpl()

  override val outgoingCallerIdList: OutgoingCallerIdReadRequestExecutor =
    new OutgoingCallerIdReadRequestExecutorImpl()

  override val outgoingCallerIdDelete: OutgoingCallerIdDeleteRequestExecutor =
    new OutgoingCallerIdDeleteRequestExecutorImpl()

  override val outgoingCallerIdCreate: OutgoingCallerIdCreateRequestExecutor =
    new OutgoingCallerIdCreateRequestExecutorImpl()
}
