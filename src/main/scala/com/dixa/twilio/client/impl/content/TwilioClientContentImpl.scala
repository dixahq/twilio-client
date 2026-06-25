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

package com.dixa.twilio.client.impl.content

import com.dixa.twilio.client.content._
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[client] final class TwilioClientContentImpl(
    implicit httpExt: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientContent {

  override val contentFetch: ContentFetchRequestExecutor =
    new ContentFetchRequestExecutorImpl()

  override val contentRead: ContentReadRequestExecutor =
    new ContentReadRequestExecutorImpl()

  override val contentDelete: ContentDeleteRequestExecutor =
    new ContentDeleteRequestExecutorImpl()

  override val contentApprovalFetch: ContentApprovalFetchRequestExecutor =
    new ContentApprovalFetchRequestExecutorImpl()

  override val contentAndApprovalsRead: ContentAndApprovalsReadRequestExecutor =
    new ContentAndApprovalsReadRequestExecutorImpl()

  override val contentAndApprovalsSearch: ContentAndApprovalsSearchRequestExecutor =
    new ContentAndApprovalsSearchRequestExecutorImpl()

  override val contentSearch: ContentSearchRequestExecutor =
    new ContentSearchRequestExecutorImpl()

}
