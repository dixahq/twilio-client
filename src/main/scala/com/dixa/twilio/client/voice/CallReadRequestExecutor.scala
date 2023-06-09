package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.Call

trait CallReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      CallReadRequestExecutor.CallReadRequest,
      CallReadRequestExecutor.CallReadException,
      Call
    ] {

  import CallReadRequestExecutor._

  override final protected type ApiExceptionWrapper = CallReadException.Api

  override final protected type UnspecifiedException = CallReadException.Unspecified
}

object CallReadRequestExecutor {

  sealed trait CallReadRequest {
    def accountSid: TwilioAccount.Sid
    def to: Option[PhoneNumberE164]
    def from: Option[PhoneNumberE164]
    def parentCallSid: Option[Call.Sid]
    def status: Option[Call.Status]
    def startTimeBefore: Option[Iso8601DateTime]
    def startTimeAfter: Option[Iso8601DateTime]
    def endTimeBefore: Option[Iso8601DateTime]
    def endTimeAfter: Option[Iso8601DateTime]
  }

  private final case class CallReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      to: Option[PhoneNumberE164],
      from: Option[PhoneNumberE164],
      parentCallSid: Option[Call.Sid],
      status: Option[Call.Status],
      startTimeBefore: Option[Iso8601DateTime],
      startTimeAfter: Option[Iso8601DateTime],
      endTimeBefore: Option[Iso8601DateTime],
      endTimeAfter: Option[Iso8601DateTime],
  ) extends CallReadRequest

  object CallReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[CallReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        to: Option[PhoneNumberE164],
        from: Option[PhoneNumberE164],
        parentCallSid: Option[Call.Sid],
        status: Option[Call.Status],
        startTimeBefore: Option[Iso8601DateTime],
        startTimeAfter: Option[Iso8601DateTime],
        endTimeBefore: Option[Iso8601DateTime],
        endTimeAfter: Option[Iso8601DateTime],
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          to,
          from,
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withTo(to: PhoneNumberE164): Builder[Attributes] =
        new Builder(
          accountSid,
          Some(to),
          from,
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withFrom(from: PhoneNumberE164): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          Some(from),
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withParentCallSid(parentCallSid: Call.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          Some(parentCallSid),
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withStatus(status: Call.Status): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          Some(status),
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withStartTimeBefore(startTime: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          Some(startTime),
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )

      def withStartTimeAfter(startTime: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeBefore,
          Some(startTime),
          endTimeBefore,
          endTimeAfter
        )

      def withEndTimeBefore(endTime: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          Some(endTime),
          endTimeAfter
        )

      def withEndTimeAfter(endTime: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          Some(endTime)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallReadRequest =
        CallReadRequestImpl(
          accountSid.get,
          to,
          from,
          parentCallSid,
          status,
          startTimeBefore,
          startTimeAfter,
          endTimeBefore,
          endTimeAfter
        )
    }

    def builder(fun: BuilderStartState => CallReadRequest): CallReadRequest =
      fun(new BuilderStartState(None, None, None, None, None, None, None, None, None))
  }

  sealed trait CallReadException extends RuntimeException
  object CallReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch conferences"
          ),
          cause.orNull
        )
        with CallReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
