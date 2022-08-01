package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  HttpEntityString,
  TwilioResponseNextPageJsonRep,
  TwilioUri
}
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.{
  OutgoingCallerIdReadException,
  OutgoingCallerIdReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.OutgoingCallerId
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[impl] class OutgoingCallerIdReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends OutgoingCallerIdReadRequestExecutor {

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: OutgoingCallerIdReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[
    Either[OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException, OutgoingCallerId]
  ] = {
    println(s"response entity: $responseEntity")
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(OutgoingCallerIdReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.outgoing_caller_ids.map { jsonReq =>
          Right(jsonReq.toModel)
        }
    }
  }

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest
  ): Either[OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException, HttpRequest] = {
    val query = {
      val friendlyNameQuery: Option[(String, String)] = req.filter.friendlyName.map {
        friendlyName =>
          "FriendlyName" -> friendlyName.toString
      }
      val phonenumberQuery: Option[(String, String)] = req.filter.phoneNumber.map { phoneNumber =>
        "PhoneNumber" -> phoneNumber.asString
      }
      Query(
        Map("PageSize" -> req.filter.pageSize.toString) ++
          List(
            friendlyNameQuery,
            phonenumberQuery
          ).flatten.toMap
      )
    }

    Right(
      TwilioPath(
        ApiSubDomain.Api,
        HttpMethods.GET,
        s"/2010-04-01/Accounts/${req.accountSid}/OutgoingCallerIds.json?${query.toString}"
      ).createHttpRequest(connSettings)
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdReadException.Api = OutgoingCallerIdReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): OutgoingCallerIdReadException.Unspecified =
    OutgoingCallerIdReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(outgoing_caller_ids: List[OutgoingCallerIdJsonRep])

  override protected def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[OutgoingCallerIdReadException, Option[HttpRequest]] =
    entityString
      .parse[TwilioResponseNextPageJsonRep]()
      .left
      .map { ex =>
        createUnspecifiedException(Some(ex.getMessage), Some(ex.cause))
      }
      .map { response =>
        response.next_page_uri.map { nextPage =>
          TwilioUri
            .autoDetect(nextPage, HttpMethods.GET, ApiSubDomain.Api)
            .createHttpRequest(connectionSettings)
        }
      }
}
