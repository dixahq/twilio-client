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

package com.dixa.twilio.client.phonenumber

import org.apache.pekko.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

/** Delete an incoming phone number from twilio account.
  *
  * An IncomingPhoneNumber instance resource represents a Twilio phone number provisioned from
  * Twilio, ported or hosted to Twilio.
  *
  * @see
  *   https://www.twilio.com/docs/phone-numbers/api/incomingphonenumber-resource#delete-an-incomingphonenumber-resource
  */
trait IncomingPhoneNumberDeleteRequestExecutor
    extends SingleRequestExecutor[
      IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest,
      IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException,
      Done,
      IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest.Builder
    ] {

  import IncomingPhoneNumberDeleteRequestExecutor._

  override protected final type ApiExceptionWrapper = IncomingPhoneNumberDeleteException.Api

  override protected final type UnspecifiedException =
    IncomingPhoneNumberDeleteException.Unspecified

  override protected final def createBuilderStartState()
      : IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest.Builder =
    IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest.Builder.empty
}

object IncomingPhoneNumberDeleteRequestExecutor {

  /** Request representation of deleting an incoming phone number.
    *
    * This request does not have any complex requirements, and hence no build function is needed.
    * You should just create a plain instance of this, via the apply method in the companion object.
    */
  final case class IncomingPhoneNumberDeleteRequest(
      accountSid: TwilioAccount.Sid,
      phoneNumberId: TwilioPhoneNumber.Sid
  )
  object IncomingPhoneNumberDeleteRequest {
    type BuilderStartState = Builder

    final class Builder private[phonenumber] (
        accountSid: Option[TwilioAccount.Sid],
        phoneNumberId: Option[TwilioPhoneNumber.Sid]
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), phoneNumberId)
      def withPhoneNumberId(phoneNumberId: TwilioPhoneNumber.Sid): Builder =
        new Builder(accountSid, Some(phoneNumberId))
      def build(): IncomingPhoneNumberDeleteRequest =
        IncomingPhoneNumberDeleteRequest(accountSid.get, phoneNumberId.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(
        fun: BuilderStartState => IncomingPhoneNumberDeleteRequest
    ): IncomingPhoneNumberDeleteRequest = fun(Builder.empty)
  }

  sealed trait IncomingPhoneNumberDeleteException extends RuntimeException
  object IncomingPhoneNumberDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IncomingPhoneNumberDeleteException
        with ApiExceptionWrapper

    final case class PhoneNumberNotFound(
        accountSid: TwilioAccount.Sid,
        phoneNumberSid: phonenumber.TwilioPhoneNumber.Sid
    ) extends RuntimeException(
          s"Incoming phone number with sid $phoneNumberSid was not found in account: $accountSid"
        )
        with IncomingPhoneNumberDeleteException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete incoming numbers"
          ),
          cause.orNull
        )
        with IncomingPhoneNumberDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
