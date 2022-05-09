package com.dixa.twilio.client.voice

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.{Call, TwilioCallSid}

trait CallUpdateRequestExecutor
    extends SingleRequestExecutor[
      CallUpdateRequestExecutor.CallUpdateRequest,
      CallUpdateRequestExecutor.CallUpdateException,
      Call
    ] {

  import CallUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = CallUpdateException.Api

  override final protected type UnspecifiedException = CallUpdateException.Unspecified
}

object CallUpdateRequestExecutor {

  final case class CallUpdateRequest(
      accountSid: TwilioAccount.Sid,
      callSid: TwilioCallSid,
      twiml: Response.Verified
      // API support a lot more fields, that could be added when needed.
  )

  sealed trait CallUpdateException extends RuntimeException
  object CallUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallUpdateException
    final case class CallNotFound(accountSid: TwilioAccount.Sid, callSid: TwilioCallSid)
        extends RuntimeException(s"Call with sid $callSid was not found in account: $accountSid")
        with CallUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update call"
          ),
          cause.orNull
        )
        with CallUpdateException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
