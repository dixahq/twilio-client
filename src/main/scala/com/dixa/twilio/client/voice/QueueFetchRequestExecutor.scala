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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Queue

trait QueueFetchRequestExecutor
    extends SingleRequestExecutor[
      QueueFetchRequestExecutor.QueueFetchRequest,
      QueueFetchRequestExecutor.QueueFetchException,
      Queue,
      QueueFetchRequestExecutor.QueueFetchRequest.BuilderStartState
    ] {

  import QueueFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = QueueFetchException.Api

  override final protected type UnspecifiedException = QueueFetchException.Unspecified

  override protected def createBuilderStartState()
      : QueueFetchRequestExecutor.QueueFetchRequest.BuilderStartState =
    QueueFetchRequestExecutor.QueueFetchRequest.Builder.empty
}

object QueueFetchRequestExecutor {

  sealed trait QueueFetchRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Queue.Sid
  }

  private final case class QueueFetchRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Queue.Sid
  ) extends QueueFetchRequest

  object QueueFetchRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[QueueFetchRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Queue.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid)

      def withSid(
          sid: Queue.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): QueueFetchRequest =
        QueueFetchRequestImpl(accountSid.get, sid.get)
    }

    def build(fun: BuilderStartState => QueueFetchRequest): QueueFetchRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None)
    }
  }

  sealed trait QueueFetchException extends RuntimeException
  object QueueFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with QueueFetchException
        with ApiExceptionWrapper

    final case class QueueNotFound(accountSid: TwilioAccount.Sid, sid: Queue.Sid)
        extends RuntimeException(s"Queue with sid $sid was not found in account: $accountSid")
        with QueueFetchException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch queue"
          ),
          cause.orNull
        )
        with QueueFetchException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
