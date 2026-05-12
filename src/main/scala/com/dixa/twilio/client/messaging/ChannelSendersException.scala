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

package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.ApiException
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper

sealed trait ChannelSendersException extends RuntimeException

object ChannelSendersException {

  final case class ChannelSenderNotSupported(sender: String)
      extends RuntimeException(s"Channel sender is not supported: $sender")
      with ChannelSendersException

  final case class ParseFailure(msg: String)
      extends RuntimeException(msg)
      with ChannelSendersException

  final case class Api(cause: ApiException)
      extends RuntimeException(cause)
      with ChannelSendersException
      with ApiExceptionWrapper

  final case class SenderIdAlreadyExists(senderId: String, apiMsg: String, apiLink: String)
      extends RuntimeException(
        s"SenderId already exists: $senderId - ${apiMsg} - ${apiLink}"
      )
      with ChannelSendersException

  final case class CouldNotExtendCreditLine(
      wabaId: Option[String],
      apiMsg: String,
      apiLink: String
  ) extends RuntimeException(
        s"Could not extend credit line for WABA: ${wabaId.getOrElse("WABA ID Not found")} - ${apiMsg} - ${apiLink}"
      )
      with ChannelSendersException

  final case class Unspecified(msg: Option[String], cause: Option[Throwable])
      extends RuntimeException(
        msg.getOrElse(
          "Unspecified error for ChannelSender resource"
        ),
        cause.orNull
      )
      with ChannelSendersException {
    def this(msg: String) = this(Some(msg), None)

    def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
  }

  final case class Unexpected(msg: Option[String], cause: Option[Throwable])
      extends RuntimeException(
        msg.getOrElse(
          "Unexpected error from Senders API"
        ),
        cause.orNull
      )
      with ChannelSendersException {
    def this(msg: String) = this(Some(msg), None)

    def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
  }

  final case class TwilioInternalError(
      errorCode: Option[Long],
      errorMessage: Option[String],
      moreInfo: Option[String],
      rawResponse: String
  ) extends RuntimeException(
        s"Twilio internal error (${errorCode.getOrElse("unknown")}): ${errorMessage.getOrElse("unknown")} - ${moreInfo.getOrElse("")}"
      )
      with ChannelSendersException
}
