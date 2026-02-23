package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}

trait OutgoingCallerIdReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest,
      OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException,
      OutgoingCallerId,
      OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = OutgoingCallerIdReadException.Api

  override protected final type UnspecifiedException = OutgoingCallerIdReadException.Unspecified

  override protected final def createBuilderStartState()
      : OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest.Builder =
    OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest.Builder.empty
}

object OutgoingCallerIdReadRequestExecutor {
  final case class OutgoingCallerIdReadRequest(
      accountSid: TwilioAccount.Sid,
      filter: OutgoingCallerIdReadRequestFilter = OutgoingCallerIdReadRequestFilter()
  )
  object OutgoingCallerIdReadRequest {
    type BuilderStartState = Builder

    final class Builder private[phonenumber] (
        accountSid: Option[TwilioAccount.Sid],
        filter: OutgoingCallerIdReadRequestFilter
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), filter)
      def withFilter(filter: OutgoingCallerIdReadRequestFilter): Builder =
        new Builder(accountSid, filter)
      def build(): OutgoingCallerIdReadRequest =
        OutgoingCallerIdReadRequest(accountSid.get, filter)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(
        None,
        OutgoingCallerIdReadRequestFilter()
      )
    }

    def build(fun: BuilderStartState => OutgoingCallerIdReadRequest): OutgoingCallerIdReadRequest =
      fun(Builder.empty)
  }

  final case class OutgoingCallerIdReadRequestFilter(
      phoneNumber: Option[PhoneNumberE164] = None,
      friendlyName: Option[OutgoingCallerId.FriendlyName] = None,
      pageSize: Int = 20
  )

  sealed trait OutgoingCallerIdReadException extends RuntimeException
  object OutgoingCallerIdReadException {

    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with OutgoingCallerIdReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with OutgoingCallerIdReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
