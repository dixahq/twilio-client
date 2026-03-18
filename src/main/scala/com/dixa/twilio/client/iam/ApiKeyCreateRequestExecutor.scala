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

package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.iam.ApiKeyCreateRequestExecutor.{KeyCreateException, KeyCreateRequest}
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.{ApiKey, ApiKeyPolicy, TwilioAccount}

/** Create a new Twilio Standard or Restricted API key for a given account.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
  */
trait ApiKeyCreateRequestExecutor
    extends SingleRequestExecutor[
      KeyCreateRequest,
      KeyCreateException,
      ApiKey with ApiKey.HasSecret,
      KeyCreateRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper  = KeyCreateException.Api
  override protected final type UnspecifiedException = KeyCreateException.Unspecified

  override protected def createBuilderStartState(): KeyCreateRequest.BuilderStartState =
    KeyCreateRequest.Builder.empty
}

object ApiKeyCreateRequestExecutor {

  sealed trait KeyCreateRequest {
    def accountSid: TwilioAccount.Sid
    def friendlyName: Option[ApiKey.FriendlyName]
    def standardKey: Boolean
    def policy: Option[Set[ApiKeyPolicy]]
  }

  private final case class KeyCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      friendlyName: Option[ApiKey.FriendlyName],
      standardKey: Boolean,
      policy: Option[Set[ApiKeyPolicy]]
  ) extends KeyCreateRequest

  object KeyCreateRequest {

    object PhantomTypes {
      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait KeyTypeSet
      sealed trait KeyTypeSetTrue  extends KeyTypeSet
      sealed trait KeyTypeSetFalse extends KeyTypeSet

      sealed trait PolicySet
      sealed trait PolicySetTrue  extends PolicySet
      sealed trait PolicySetFalse extends PolicySet

      sealed trait DisallowPolicy
      sealed trait DisallowPolicyTrue  extends DisallowPolicy
      sealed trait DisallowPolicyFalse extends DisallowPolicy

      sealed trait PolicyRequired      extends PolicySet
      sealed trait PolicyRequiredTrue  extends PolicyRequired
      sealed trait PolicyRequiredFalse extends PolicyRequired

      sealed trait ValidPolicyCombinations[
          PR <: PhantomTypes.PolicyRequired,
          PS <: PhantomTypes.PolicySet
      ]
      object ValidPolicyCombinations {
        // Case 1: Standard Key (Policy not required, Policy not set)
        implicit val standard: ValidPolicyCombinations[
          PhantomTypes.PolicyRequiredFalse,
          PhantomTypes.PolicySetFalse
        ] =
          new ValidPolicyCombinations[
            PhantomTypes.PolicyRequiredFalse,
            PhantomTypes.PolicySetFalse
          ] {}

        // Case 2: Restricted Key (Policy required, Policy IS set)
        implicit val restricted
            : ValidPolicyCombinations[PhantomTypes.PolicyRequiredTrue, PhantomTypes.PolicySetTrue] =
          new ValidPolicyCombinations[
            PhantomTypes.PolicyRequiredTrue,
            PhantomTypes.PolicySetTrue
          ] {}
      }
    }

    import PhantomTypes._

    type BuilderStartState =
      Builder[
        AccountSidSetFalse,
        KeyTypeSetFalse,
        PolicySetFalse,
        DisallowPolicyFalse,
        PolicyRequiredFalse
      ]

    final class Builder[
        AS <: PhantomTypes.AccountSidSet,
        KT <: PhantomTypes.KeyTypeSet,
        PS <: PhantomTypes.PolicySet,
        DP <: PhantomTypes.DisallowPolicy,
        PR <: PhantomTypes.PolicyRequired
    ] private[KeyCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        friendlyName: Option[ApiKey.FriendlyName],
        standardKey: Boolean,
        policy: Option[Set[ApiKeyPolicy]]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[AccountSidSetTrue, KT, PS, DP, PR] =
        new Builder(Some(accountSid), friendlyName, standardKey, policy)

      def withFriendlyName(
          friendlyName: ApiKey.FriendlyName
      ): Builder[AS, KT, PS, DP, PR] =
        new Builder(accountSid, Some(friendlyName), standardKey, policy)

      /** Create a standard key
        *
        * Only Standard keys can be used to generate Access Tokens for client-side SDKs such as the
        * Twilio Voice JS SDK. Restricted keys cannot be used for Access Token generation.
        *
        * @see
        *   https://www.twilio.com/docs/iam/api-keys/restricted-api-keys
        */
      def withTypeStandard()(
          implicit ev: PS =:= PolicySetFalse
      ): Builder[AS, KeyTypeSetTrue, PS, DisallowPolicyTrue, PR] =
        new Builder(accountSid, friendlyName, true, policy)

      def withTypeRestricted(): Builder[AS, KeyTypeSetTrue, PS, DP, PolicyRequiredTrue] =
        new Builder(accountSid, friendlyName, false, policy)

      def withPolicy(policy: Set[ApiKeyPolicy])(
          implicit ev: DP =:= DisallowPolicyFalse
      ): Builder[AS, KT, PolicySetTrue, DP, PR] =
        new Builder(accountSid, friendlyName, standardKey, Some(policy))

      def build()(
          implicit evAccount: AS =:= AccountSidSetTrue,
          evKeyType: KT =:= KeyTypeSetTrue,
          evValidPolicyCombo: ValidPolicyCombinations[PR, PS]
      ): KeyCreateRequest =
        KeyCreateRequestImpl(accountSid.get, friendlyName, standardKey, policy)
    }

    def build(fun: BuilderStartState => KeyCreateRequest): KeyCreateRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new Builder(None, None, true, None)
    }
  }

  sealed trait KeyCreateException extends RuntimeException

  object KeyCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with KeyCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to create API key"),
          cause.orNull
        )
        with KeyCreateException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
