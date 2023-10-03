package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor.{
  IncomingNumbersReadException,
  IncomingNumbersReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import java.net.URLEncoder
import scala.concurrent.ExecutionContext

private[impl] class IncomingNumbersReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends IncomingNumbersReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest
  ): Either[IncomingNumbersReadException, HttpRequest] = {
    val filterQueryParam = req.filter
      .map { f =>
        val filterEscaped = URLEncoder.encode(f.toString, "UTF-8")
        s"&PhoneNumber=$filterEscaped"
      }
      .getOrElse("")
    val pathAsString =
      s"/2010-04-01/Accounts/${connSettings.accountSid}/IncomingPhoneNumbers.json?PageSize=1000$filterQueryParam"
    createHttpRequestFor(pathAsString, connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    IncomingNumbersReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = IncomingNumbersReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(incoming_phone_numbers: List[IncomingPhoneNumberJsonRep])

  private implicit val outerJsonRepReader: Reader[OuterJsonRep] =
    macroR[OuterJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: IncomingNumbersReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[IncomingNumbersReadException, TwilioIncomingPhoneNumber]] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(IncomingNumbersReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.incoming_phone_numbers.map { jsonRep =>
          Right(jsonRep.toModel)
        }
    }
  }
}
