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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference

trait ConferenceReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ConferenceReadRequestExecutor.ConferenceReadRequest,
      ConferenceReadRequestExecutor.ConferenceReadException,
      Conference,
      ConferenceReadRequestExecutor.ConferenceReadRequest.BuilderStartState
    ] {

  import ConferenceReadRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceReadException.Api

  override final protected type UnspecifiedException = ConferenceReadException.Unspecified

  override protected def createBuilderStartState()
      : ConferenceReadRequestExecutor.ConferenceReadRequest.BuilderStartState =
    ConferenceReadRequestExecutor.ConferenceReadRequest.Builder.empty
}

object ConferenceReadRequestExecutor {

  /** A request to the Twilio Conferences list endpoint.
    *
    * `status` is required. As of 2026-07-13 Twilio stores in-progress and completed conferences in
    * separate systems, so a single list call can only ever return conferences of one status — there
    * is no "all statuses" query. The status is therefore a routing discriminator rather than an
    * optional filter, and callers must state which conferences they want.
    */
  sealed trait ConferenceReadRequest {
    def accountSid: TwilioAccount.Sid
    def dateCreated: Option[Iso8601DateTime]
    def dateUpdated: Option[Iso8601DateTime]
    def friendlyName: Option[Conference.FriendlyName]
    def status: Conference.Status
  }

  private final case class ConferenceReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      dateCreated: Option[Iso8601DateTime],
      dateUpdated: Option[Iso8601DateTime],
      friendlyName: Option[Conference.FriendlyName],
      status: Conference.Status
  ) extends ConferenceReadRequest

  object ConferenceReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestStatusAttribute     extends RequestAttribute

    type RequestRequiredAttributes =
      RequestAttribute with RequestAccountSidAttribute with RequestStatusAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[ConferenceReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        dateCreated: Option[Iso8601DateTime],
        dateUpdated: Option[Iso8601DateTime],
        friendlyName: Option[Conference.FriendlyName],
        status: Option[Conference.Status]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), dateCreated, dateUpdated, friendlyName, status)

      def withDateCreated(dateCreated: Iso8601DateTime): Builder[Attributes] =
        new Builder(accountSid, Some(dateCreated), dateUpdated, friendlyName, status)

      def withDateUpdated(dateUpdated: Iso8601DateTime): Builder[Attributes] =
        new Builder(accountSid, dateCreated, Some(dateUpdated), friendlyName, status)

      def withFriendlyName(friendlyName: Conference.FriendlyName): Builder[Attributes] =
        new Builder(accountSid, dateCreated, dateUpdated, Some(friendlyName), status)

      /** Required — see [[ConferenceReadRequest]]. */
      def withStatus(
          status: Conference.Status
      ): Builder[Attributes with RequestStatusAttribute] =
        new Builder(accountSid, dateCreated, dateUpdated, friendlyName, Some(status))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceReadRequest =
        ConferenceReadRequestImpl(
          accountSid.get,
          dateCreated,
          dateUpdated,
          friendlyName,
          status.get
        )
    }

    def build(fun: BuilderStartState => ConferenceReadRequest): ConferenceReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait ConferenceReadException extends RuntimeException
  object ConferenceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch conferences"
          ),
          cause.orNull
        )
        with ConferenceReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
