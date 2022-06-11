package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioUri}
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor.{
  ActiveNumbersReadException,
  ActiveNumbersReadRequest
}
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.TwilioActivePhoneNumber
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[impl] class ActiveNumbersReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ActiveNumbersReadRequestExecutor {

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest
  ): Either[ActiveNumbersReadException, HttpRequest] = {
    val pathAsString =
      s"/Numbers/ActiveNumbers/${req.phoneNumberSid.map(_.asString).getOrElse("")}?PageSize=1000"
    Right(
      TwilioPath(ApiSubDomain.Preview, HttpMethods.GET, pathAsString)
        .createHttpRequest(connSettings)
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ActiveNumbersReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = ActiveNumbersReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(items: List[ActivePhoneNumberJsonRep])

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ActiveNumbersReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ActiveNumbersReadException, TwilioActivePhoneNumber]] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ActiveNumbersReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.items.map { jsonRep =>
          Right(jsonRep.toModel)
        }
    }
  }

  //  Full meta json object likes like this, but for now we only need the nex_page_url:
  //  "meta": {
  //    "page": 1,
  //    "page_size": 2,
  //    "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
  //    "previous_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0&PageToken=PTMGd8410e59416697cb4455c87eba98a6d0",
  //    "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=1&PageToken=PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd",
  //    "next_page_url": null,
  //    "key": "services"
  //  }
  private case class MetaJsonRep(next_page_url: Option[String])
  private case class MetaRootJsonResp(meta: MetaJsonRep)

  override protected def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[UnspecifiedException, Option[HttpRequest]] = {
    entityString
      .parse[MetaRootJsonResp]()
      .left
      .map { ex =>
        createUnspecifiedException(Some(ex.getMessage), Some(ex.cause))
      }
      .map { response =>
        response.meta.next_page_url.map { nextPage =>
          TwilioUri
            .autoDetect(nextPage, HttpMethods.GET, ApiSubDomain.Preview)
            .createHttpRequest(connectionSettings)
        }
      }
  }
}
