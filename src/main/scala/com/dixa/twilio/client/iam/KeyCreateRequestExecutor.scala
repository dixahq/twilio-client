package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.iam.KeyCreateRequestExecutor.{KeyCreateException, KeyCreateRequest}
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.iam.{ApiKey, TwilioAccount}

/** Create a new Twilio Standard or Restricted API key for a given account.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
  */
trait KeyCreateRequestExecutor
    extends SingleRequestExecutor[
      KeyCreateRequest,
      KeyCreateException,
      ApiKey,
      KeyCreateRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper  = KeyCreateException.Api
  override protected final type UnspecifiedException = KeyCreateException.Unspecified

  override protected def createBuilderStartState(): KeyCreateRequest.BuilderStartState =
    KeyCreateRequest.Builder.empty
}

object KeyCreateRequestExecutor {

  sealed trait KeyCreateRequest {
    def accountSid: TwilioAccount.Sid
    def friendlyName: Option[ApiKey.FriendlyName]
    def keyType: Option[KeyCreateRequestExecutor.KeyType]
  }

  private final case class KeyCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      friendlyName: Option[ApiKey.FriendlyName],
      keyType: Option[KeyCreateRequestExecutor.KeyType]
  ) extends KeyCreateRequest

  object KeyCreateRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.RequestAccountSidAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[KeyCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        friendlyName: Option[ApiKey.FriendlyName],
        keyType: Option[KeyCreateRequestExecutor.KeyType]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(Some(accountSid), friendlyName, keyType)

      def withFriendlyName(friendlyName: ApiKey.FriendlyName): Builder[Attributes] =
        new Builder(accountSid, Some(friendlyName), keyType)

      def withKeyType(keyType: KeyCreateRequestExecutor.KeyType): Builder[Attributes] =
        new Builder(accountSid, friendlyName, Some(keyType))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): KeyCreateRequest =
        KeyCreateRequestImpl(accountSid.get, friendlyName, keyType)
    }

    def build(fun: BuilderStartState => KeyCreateRequest): KeyCreateRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None)
    }
  }

  /** The type of Twilio API key to create.
    *
    * Only Standard keys can be used to generate Access Tokens for client-side SDKs such as the
    * Twilio Voice JS SDK. Restricted keys cannot be used for Access Token generation.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api-keys/restricted-api-keys
    */
  sealed abstract class KeyType(override val twilioString: String) extends TwilioStringValue

  object KeyType {
    case object Standard   extends KeyType("standard")
    case object Restricted extends KeyType("restricted")
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
