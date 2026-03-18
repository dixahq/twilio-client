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

trait QueueUpdateRequestExecutor
    extends SingleRequestExecutor[
      QueueUpdateRequestExecutor.QueueUpdateRequest,
      QueueUpdateRequestExecutor.QueueUpdateException,
      Queue,
      QueueUpdateRequestExecutor.QueueUpdateRequest.BuilderStartState
    ] {

  import QueueUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = QueueUpdateException.Api

  override final protected type UnspecifiedException = QueueUpdateException.Unspecified

  override protected def createBuilderStartState()
      : QueueUpdateRequestExecutor.QueueUpdateRequest.BuilderStartState =
    QueueUpdateRequestExecutor.QueueUpdateRequest.Builder.empty
}

object QueueUpdateRequestExecutor {

  sealed trait QueueUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Queue.Sid
    def friendlyName: Option[Queue.FriendlyName]
    def maxSize: Option[Queue.MaxSize]
  }

  private final case class QueueUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Queue.Sid,
      friendlyName: Option[Queue.FriendlyName],
      maxSize: Option[Queue.MaxSize]
  ) extends QueueUpdateRequest

  object QueueUpdateRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[QueueUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Queue.Sid],
        friendlyName: Option[Queue.FriendlyName],
        maxSize: Option[Queue.MaxSize]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid, friendlyName, maxSize)

      def withSid(
          sid: Queue.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid), friendlyName, maxSize)

      def withFriendlyName(friendlyName: Queue.FriendlyName): Builder[Attributes] =
        new Builder(accountSid, sid, Some(friendlyName), maxSize)

      /** Set the Max size to update to.
        *
        * Not that only a specific range of values are allowed here. At time of writing this is
        * 1-5000. If you specify something outside of the allowed range, you request will fail at
        * runtime.
        */
      def withMaxSize(maxSize: Queue.MaxSize): Builder[Attributes] =
        new Builder(accountSid, sid, friendlyName, Some(maxSize))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): QueueUpdateRequest =
        QueueUpdateRequestImpl(accountSid.get, sid.get, friendlyName, maxSize)
    }

    def build(fun: BuilderStartState => QueueUpdateRequest): QueueUpdateRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None)
    }
  }

  sealed trait QueueUpdateException extends RuntimeException
  object QueueUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with QueueUpdateException
        with ApiExceptionWrapper

    final case class QueueNotFound(accountSid: TwilioAccount.Sid, sid: Queue.Sid)
        extends RuntimeException(s"Queue with sid $sid was not found in account: $accountSid")
        with QueueUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update queue"
          ),
          cause.orNull
        )
        with QueueUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
