package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.model.messaging.TwilioMessagingService

trait ServicesReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ServicesReadRequestExecutor.ServicesReadRequest,
      ServicesReadRequestExecutor.ServicesReadException,
      TwilioMessagingService,
      ServicesReadRequestExecutor.ServicesReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = ServicesReadException.Api

  override protected final type UnspecifiedException = ServicesReadException.Unspecified

  override protected final def createBuilderStartState()
      : ServicesReadRequestExecutor.ServicesReadRequest.Builder =
    ServicesReadRequestExecutor.ServicesReadRequest.Builder.empty
}

object ServicesReadRequestExecutor {
  final case class ServicesReadRequest()
  object ServicesReadRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] () {
      def build(): ServicesReadRequest = ServicesReadRequest()
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState()
    }

    def build(fun: BuilderStartState => ServicesReadRequest): ServicesReadRequest =
      fun(Builder.empty)
  }

  sealed trait ServicesReadException extends RuntimeException
  object ServicesReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ServicesReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read services"
          ),
          cause.orNull
        )
        with ServicesReadException
  }
}
