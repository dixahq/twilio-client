package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.client.{messaging, ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.messaging.TwilioMessagingService
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[impl] class ServicesReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ServicesReadRequestExecutor {

  /** Specify the sub domain in twilio, that this API request is against. */
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  /** Specify the Http method that this API request uses */
  override protected def method: HttpMethod = HttpMethods.GET
  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: messaging.ServicesReadRequestExecutor.ServicesReadRequest
  ): Either[ServicesReadException, HttpRequest] =
    createHttpRequestFor("/v1/Services?PageSize=1000", connSettings)

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ServicesReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ServicesReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(services: List[MessagingServiceJsonRep])

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ServicesReadRequestExecutor.ServicesReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ServicesReadException, TwilioMessagingService]] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ServicesReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.services.map { jsonRep =>
          Right(jsonRep.toTwilioMessagingService)
        }
    }
  }
}
